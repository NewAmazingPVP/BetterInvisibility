package newamazingpvp.betterinvisibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class BetterInvisibility extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;
    private FileConfiguration config;
    private boolean isEffectAddedByPlugin = false;
    private final HashMap<UUID, Long> lastHitTimestamps = new HashMap<>();

    @Override
    public void onEnable() {
        new Metrics(this, 18558);
        // Initialize ProtocolManager
        protocolManager = ProtocolLibrary.getProtocolManager();
        getServer().getPluginManager().registerEvents(this, this);
        loadConfiguration();
        config = getConfig();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        removeAllArmor(event.getPlayer());
                    } else {
                        restoreArmor(event.getPlayer());
                        this.cancel();
                    }
                }
            };
            runnable.runTaskTimer(this, 0L, 1L);
        }
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (isEffectAddedByPlugin) {
            isEffectAddedByPlugin = false;
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (event.getNewEffect() != null && event.getNewEffect().getType() != null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                if(config.getBoolean("hide.potionParticles")) {
                    PotionEffect potion = event.getNewEffect();
                    PotionEffect newEffect = new PotionEffect(PotionEffectType.INVISIBILITY, potion.getDuration(), potion.getAmplifier(), potion.isAmbient(), false);
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    isEffectAddedByPlugin = true;
                    player.addPotionEffect(newEffect);
                    isEffectAddedByPlugin = true;
                }
                Bukkit.getScheduler().runTaskTimer(this, () -> removeAllArmor(player), 0L, 1L);

            } else if (event.getOldEffect() != null && event.getOldEffect().getType() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                Bukkit.getScheduler().runTaskTimer(this, () -> restoreArmor(player), 0L, 1L);
            }
        }
    }


    public void restoreArmor(Player player) {
        PacketContainer restoreArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        restoreArmorPacket.getIntegers().write(0, player.getEntityId());

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        ArrayList<EnumWrappers.ItemSlot> slots = new ArrayList<>();

        if (config.getBoolean("hide.boots")) {
            slots.add(EnumWrappers.ItemSlot.FEET);
        }
        if (config.getBoolean("hide.leggings")) {
            slots.add(EnumWrappers.ItemSlot.LEGS);
        }
        if (config.getBoolean("hide.chestplate")) {
            slots.add(EnumWrappers.ItemSlot.CHEST);
        }
        if (config.getBoolean("hide.helmet")) {
            slots.add(EnumWrappers.ItemSlot.HEAD);
        }
        if (config.getBoolean("hide.mainhand")) {
            slots.add(EnumWrappers.ItemSlot.MAINHAND);
        }
        if (config.getBoolean("hide.offhand")) {
            slots.add(EnumWrappers.ItemSlot.OFFHAND);
        }

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slotItemPairs = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            EnumWrappers.ItemSlot itemSlot = slots.get(i);
            ItemStack item;
            if (itemSlot == EnumWrappers.ItemSlot.MAINHAND) {
                item = mainHand;
            } else if (itemSlot == EnumWrappers.ItemSlot.OFFHAND) {
                item = offHand;
            } else {
                item = armorContents[i];
            }
            Pair<EnumWrappers.ItemSlot, ItemStack> slotItemPair = new Pair<>(itemSlot, item);
            slotItemPairs.add(slotItemPair);
        }
        restoreArmorPacket.getSlotStackPairLists().write(0, slotItemPairs);
        List<Player> playersInWorld = player.getWorld().getPlayers();
        for (Player currentPlayer : playersInWorld) {
            try {
                if (!currentPlayer.equals(player)) {
                    protocolManager.sendServerPacket(currentPlayer, restoreArmorPacket);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) throws InvocationTargetException {
        if(!config.getBoolean("enable_workaround")){
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {

                event.setCancelled(true);

                long currentTime = System.currentTimeMillis();
                long lastHitTime = lastHitTimestamps.getOrDefault(player.getUniqueId(), 0L);
                long cooldownMillis = 500;

                if (currentTime - lastHitTime < cooldownMillis) {
                    return;
                }

                lastHitTimestamps.put(player.getUniqueId(), currentTime);

                double damage = event.getFinalDamage();

                player.setHealth(Math.max(0, player.getHealth() - damage));

                Location attackerLocation = attacker.getLocation();
                Location targetLocation = player.getLocation();

                Vector knockbackDirection = targetLocation.toVector().subtract(attackerLocation.toVector()).normalize();

                double knockbackMagnitude = attacker.isSprinting() ? 1.3 : 0.8;

                Vector horizontalKnockback = knockbackDirection.multiply(knockbackMagnitude);

                Vector verticalKnockback = new Vector(0, 0.35, 0);

                Vector knockback = horizontalKnockback.add(verticalKnockback);

                player.setVelocity(knockback);
            }
        }
    }

    public void removeAllArmor(Player player) {
        PacketContainer clearArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        clearArmorPacket.getIntegers().write(0, player.getEntityId());

        ArrayList<EnumWrappers.ItemSlot> slots = new ArrayList<>();

        if (config.getBoolean("hide.boots")) {
            slots.add(EnumWrappers.ItemSlot.FEET);
        }
        if (config.getBoolean("hide.leggings")) {
            slots.add(EnumWrappers.ItemSlot.LEGS);
        }
        if (config.getBoolean("hide.chestplate")) {
            slots.add(EnumWrappers.ItemSlot.CHEST);
        }
        if (config.getBoolean("hide.helmet")) {
            slots.add(EnumWrappers.ItemSlot.HEAD);
        }
        if (config.getBoolean("hide.mainhand")) {
            slots.add(EnumWrappers.ItemSlot.MAINHAND);
        }
        if (config.getBoolean("hide.offhand")) {
            slots.add(EnumWrappers.ItemSlot.OFFHAND);
        }

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slotItemPairs = new ArrayList<>();
        for (EnumWrappers.ItemSlot itemSlot : slots) {
            ItemStack airItem = new ItemStack(Material.AIR);
            Pair<EnumWrappers.ItemSlot, ItemStack> slotItemPair = new Pair<>(itemSlot, airItem);
            slotItemPairs.add(slotItemPair);
        }
        clearArmorPacket.getSlotStackPairLists().write(0, slotItemPairs);
        List<Player> playersInWorld = player.getWorld().getPlayers();
        for (Player currentPlayer : playersInWorld) {
            try {
                if (!currentPlayer.equals(player)) {
                    protocolManager.sendServerPacket(currentPlayer, clearArmorPacket);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void loadConfiguration(){
        saveDefaultConfig();

        config = getConfig();
        config.addDefault("hide.helmet", true);
        config.addDefault("hide.chestplate", true);
        config.addDefault("hide.leggings", true);
        config.addDefault("hide.boots", true);
        config.addDefault("hide.mainhand", true);
        config.addDefault("hide.offhand", true);
        config.addDefault("hide.potionParticles", false);
        config.addDefault("enable_workaround", false);

        config.options().copyDefaults(true);
        saveConfig();
    }
}
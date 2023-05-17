package newamazingpvp.betterinvisibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BetterInvisibility extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        // Initialize ProtocolManager
        protocolManager = ProtocolLibrary.getProtocolManager();
        // Register events for this plugin
        getServer().getPluginManager().registerEvents(this, this);
        registerPacketListener();
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (event.getNewEffect() != null && event.getNewEffect().getType() != null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                // Schedule a task to remove all armor when player becomes invisible
                Bukkit.getScheduler().runTaskTimer(this, () -> removeAllArmor(player), 0L, 0L);
            } else if (event.getOldEffect() != null && event.getOldEffect().getType() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                // Schedule a task to restore player's armor when invisibility effect is removed
                Bukkit.getScheduler().runTaskTimer(this, () -> restoreArmor(player), 0L, 0L);
            }
        }
    }


    public void restoreArmor(Player player) {
        // Create a packet to restore player's armor
        PacketContainer restoreArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        restoreArmorPacket.getIntegers().write(0, player.getEntityId());

        // Get the player's armor contents and held items
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // Define the slots where the armor and held items are equipped
        EnumWrappers.ItemSlot[] slots = new EnumWrappers.ItemSlot[]{
                EnumWrappers.ItemSlot.FEET,
                EnumWrappers.ItemSlot.LEGS,
                EnumWrappers.ItemSlot.CHEST,
                EnumWrappers.ItemSlot.HEAD,
                EnumWrappers.ItemSlot.MAINHAND,
                EnumWrappers.ItemSlot.OFFHAND
        };

        // Create a list of slot-item pairs for the packet
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slotItemPairs = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            EnumWrappers.ItemSlot itemSlot = slots[i];
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
        // Send the restore armor packet to all players in the same world
        List<Player> playersInWorld = player.getWorld().getPlayers();
        for (Player currentPlayer : playersInWorld) {
            try {
                if (currentPlayer != player) {
                    protocolManager.sendServerPacket(currentPlayer, restoreArmorPacket);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        System.out.println("onEntityDamageByEntity");
        if (event.getEntity() instanceof Player) {
            // Get the player who is taking damage
            Player player = (Player) event.getEntity();

            // Calculate the damage, including enchantments and armor reduction
            double damage = event.getFinalDamage();

            // Manually apply the damage to the player without showing the animation
            player.setHealth(Math.max(0, player.getHealth() - damage));

            // Cancel the event to prevent the animation from being shown
            event.setCancelled(true);
        }
    }



    public void removeAllArmor(Player player) {
        // Create a packet to clear player's armor
        PacketContainer clearArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        clearArmorPacket.getIntegers().write(0, player.getEntityId());

        // Define the slots where the armor and held items are equipped
        List<EnumWrappers.ItemSlot> slots = new ArrayList<>(Arrays.asList(
                EnumWrappers.ItemSlot.FEET,
                EnumWrappers.ItemSlot.LEGS,
                EnumWrappers.ItemSlot.CHEST,
                EnumWrappers.ItemSlot.HEAD,
                EnumWrappers.ItemSlot.MAINHAND,
                EnumWrappers.ItemSlot.OFFHAND
        ));

        // Create a list of slot-item pairs with empty items (air) for the packet
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slotItemPairs = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            EnumWrappers.ItemSlot itemSlot = slots.get(i);
            ItemStack airItem = new ItemStack(Material.AIR);
            Pair<EnumWrappers.ItemSlot, ItemStack> slotItemPair = new Pair<>(itemSlot, airItem);
            slotItemPairs.add(slotItemPair);
        }
        clearArmorPacket.getSlotStackPairLists().write(0, slotItemPairs);

        // Send the clear armor packet to all players in the same world
        List<Player> playersInWorld = player.getWorld().getPlayers();
        for (Player currentPlayer : playersInWorld) {
            try {
                if (currentPlayer != player) {
                    protocolManager.sendServerPacket(currentPlayer, clearArmorPacket);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void registerPacketListener() {
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ANIMATION) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        System.out.println("onPacketSending");
                            event.setCancelled(true);
                    }
                }
        );
    }
}
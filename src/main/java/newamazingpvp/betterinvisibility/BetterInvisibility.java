package newamazingpvp.betterinvisibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BetterInvisibility extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            PotionEffectType newEffectType = event.getNewEffect().getType();
            PotionEffectType oldEffectType = event.getOldEffect().getType();

            if (newEffectType.equals(PotionEffectType.INVISIBILITY)) {
                Bukkit.getScheduler().runTaskTimer(this, () -> removeAllArmor(player), 0L, 0L);
            } else if (oldEffectType.equals(PotionEffectType.INVISIBILITY)) {
                Bukkit.getScheduler().runTaskTimer(this, () -> restoreArmor(player), 0L, 0L);
            }
        }
    }

    public void restoreArmor(Player player) {
        PacketContainer restoreArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        restoreArmorPacket.getIntegers().write(0, player.getEntityId());

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        EnumWrappers.ItemSlot[] slots = new EnumWrappers.ItemSlot[] {
                EnumWrappers.ItemSlot.FEET,
                EnumWrappers.ItemSlot.LEGS,
                EnumWrappers.ItemSlot.CHEST,
                EnumWrappers.ItemSlot.HEAD,
                EnumWrappers.ItemSlot.MAINHAND,
                EnumWrappers.ItemSlot.OFFHAND
        };

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slotItemPairs = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            EnumWrappers.ItemSlot itemSlot = slots[i];
            ItemStack item = null;
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
                if (currentPlayer != player) {
                    protocolManager.sendServerPacket(currentPlayer, restoreArmorPacket);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void removeAllArmor(Player player) {
        PacketContainer clearArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        clearArmorPacket.getIntegers().write(0, player.getEntityId());

        List<EnumWrappers.ItemSlot> slots = new ArrayList<>(Arrays.asList(EnumWrappers.ItemSlot.FEET, EnumWrappers.ItemSlot.LEGS, EnumWrappers.ItemSlot.CHEST, EnumWrappers.ItemSlot.HEAD, EnumWrappers.ItemSlot.MAINHAND, EnumWrappers.ItemSlot.OFFHAND));
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slotItemPairs = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            EnumWrappers.ItemSlot itemSlot = slots.get(i);
            ItemStack airItem = new ItemStack(Material.AIR);
            Pair<EnumWrappers.ItemSlot, ItemStack> slotItemPair = new Pair<>(itemSlot, airItem);
            slotItemPairs.add(slotItemPair);
        }
        clearArmorPacket.getSlotStackPairLists().write(0, slotItemPairs);

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
}

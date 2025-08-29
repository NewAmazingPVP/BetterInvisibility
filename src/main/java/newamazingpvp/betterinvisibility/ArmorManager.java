package newamazingpvp.betterinvisibility;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorManager {

    private static final ItemStack AIR_ITEM = toPE(new org.bukkit.inventory.ItemStack(Material.AIR));

    private final ConfigManager configManager;
    private final Set<Integer> invisible = ConcurrentHashMap.newKeySet();

    public ArmorManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void hidePlayer(Player player) {
        invisible.add(player.getEntityId());
        sendEquipment(player, true);
    }

    public void showPlayer(Player player) {
        sendEquipment(player, false);
        invisible.remove(player.getEntityId());
    }

    public void stopTracking(Player player) {
        invisible.remove(player.getEntityId());
    }

    public boolean isInvisible(int entityId) {
        return invisible.contains(entityId);
    }

    public void maskPacket(WrapperPlayServerEntityEquipment packet) {
        List<Equipment> equipment = new ArrayList<>();

        if (configManager.isHideBoots()) {
            equipment.add(new Equipment(EquipmentSlot.BOOTS, AIR_ITEM));
        }
        if (configManager.isHideLeggings()) {
            equipment.add(new Equipment(EquipmentSlot.LEGGINGS, AIR_ITEM));
        }
        if (configManager.isHideChestplate()) {
            equipment.add(new Equipment(EquipmentSlot.CHEST_PLATE, AIR_ITEM));
        }
        if (configManager.isHideHelmet()) {
            equipment.add(new Equipment(EquipmentSlot.HELMET, AIR_ITEM));
        }
        if (configManager.isHideMainhand()) {
            equipment.add(new Equipment(EquipmentSlot.MAIN_HAND, AIR_ITEM));
        }
        if (configManager.isHideOffhand()) {
            equipment.add(new Equipment(EquipmentSlot.OFF_HAND, AIR_ITEM));
        }

        packet.setEquipment(equipment);
    }

    private void sendEquipment(Player player, boolean air) {
        List<Equipment> equipment = new ArrayList<>();

        if (configManager.isHideBoots()) {
            equipment.add(new Equipment(EquipmentSlot.BOOTS, air ? AIR_ITEM : toPE(player.getInventory().getBoots())));
        }
        if (configManager.isHideLeggings()) {
            equipment.add(new Equipment(EquipmentSlot.LEGGINGS, air ? AIR_ITEM : toPE(player.getInventory().getLeggings())));
        }
        if (configManager.isHideChestplate()) {
            equipment.add(new Equipment(EquipmentSlot.CHEST_PLATE, air ? AIR_ITEM : toPE(player.getInventory().getChestplate())));
        }
        if (configManager.isHideHelmet()) {
            equipment.add(new Equipment(EquipmentSlot.HELMET, air ? AIR_ITEM : toPE(player.getInventory().getHelmet())));
        }
        if (configManager.isHideMainhand()) {
            org.bukkit.inventory.ItemStack main = getMainHand(player);
            equipment.add(new Equipment(EquipmentSlot.MAIN_HAND, air ? AIR_ITEM : toPE(main)));
        }
        if (configManager.isHideOffhand()) {
            org.bukkit.inventory.ItemStack off = getOffHand(player);
            if (off != null) {
                equipment.add(new Equipment(EquipmentSlot.OFF_HAND, air ? AIR_ITEM : toPE(off)));
            } else if (air) {
                equipment.add(new Equipment(EquipmentSlot.OFF_HAND, AIR_ITEM));
            }
        }

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(player.getEntityId(), equipment);
        sendToWorldViewers(player, packet);
    }

    private void sendToWorldViewers(Player subject, WrapperPlayServerEntityEquipment packet) {
        for (Player viewer : subject.getWorld().getPlayers()) {
            if (viewer.equals(subject)) continue;
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        }
    }

    private static ItemStack toPE(org.bukkit.inventory.ItemStack bukkitItem) {
        if (bukkitItem == null) {
            return AIR_ITEM;
        }
        return SpigotConversionUtil.fromBukkitItemStack(bukkitItem);
    }

    private static org.bukkit.inventory.ItemStack getMainHand(Player player) {
        try {
            return player.getInventory().getItemInMainHand();
        } catch (NoSuchMethodError e) {
            return player.getInventory().getItemInHand();
        }
    }

    private static org.bukkit.inventory.ItemStack getOffHand(Player player) {
        try {
            return player.getInventory().getItemInOffHand();
        } catch (NoSuchMethodError e) {
            return null;
        }
    }
}

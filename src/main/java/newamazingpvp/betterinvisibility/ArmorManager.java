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

public class ArmorManager {

    private final ConfigManager configManager;

    public ArmorManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void restoreArmor(Player player) {
        List<Equipment> equipment = new ArrayList<>();

        if (configManager.isHideBoots()) {
            equipment.add(new Equipment(EquipmentSlot.BOOTS, toPE(player.getInventory().getBoots())));
        }
        if (configManager.isHideLeggings()) {
            equipment.add(new Equipment(EquipmentSlot.LEGGINGS, toPE(player.getInventory().getLeggings())));
        }
        if (configManager.isHideChestplate()) {
            equipment.add(new Equipment(EquipmentSlot.CHEST_PLATE, toPE(player.getInventory().getChestplate())));
        }
        if (configManager.isHideHelmet()) {
            equipment.add(new Equipment(EquipmentSlot.HELMET, toPE(player.getInventory().getHelmet())));
        }
        if (configManager.isHideMainhand()) {
            equipment.add(new Equipment(EquipmentSlot.MAIN_HAND, toPE(player.getInventory().getItemInMainHand())));
        }
        if (configManager.isHideOffhand()) {
            equipment.add(new Equipment(EquipmentSlot.OFF_HAND, toPE(player.getInventory().getItemInOffHand())));
        }

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(player.getEntityId(), equipment);
        sendToWorldViewers(player, packet);
    }

    public void removeAllArmor(Player player) {
        List<Equipment> equipment = new ArrayList<>();

        if (configManager.isHideBoots()) {
            equipment.add(new Equipment(EquipmentSlot.BOOTS, air()));
        }
        if (configManager.isHideLeggings()) {
            equipment.add(new Equipment(EquipmentSlot.LEGGINGS, air()));
        }
        if (configManager.isHideChestplate()) {
            equipment.add(new Equipment(EquipmentSlot.CHEST_PLATE, air()));
        }
        if (configManager.isHideHelmet()) {
            equipment.add(new Equipment(EquipmentSlot.HELMET, air()));
        }
        if (configManager.isHideMainhand()) {
            equipment.add(new Equipment(EquipmentSlot.MAIN_HAND, air()));
        }
        if (configManager.isHideOffhand()) {
            equipment.add(new Equipment(EquipmentSlot.OFF_HAND, air()));
        }

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(player.getEntityId(), equipment);
        sendToWorldViewers(player, packet);
    }

    private void sendToWorldViewers(Player subject, WrapperPlayServerEntityEquipment packet) {
        for (Player viewer : subject.getWorld().getPlayers()) {
            if (viewer.equals(subject)) continue;
            PacketEvents.getAPI().getProtocolManager().sendPacket(viewer, packet);
        }
    }

    private static ItemStack air() {
        return toPE(new org.bukkit.inventory.ItemStack(Material.AIR));
    }

    private static ItemStack toPE(org.bukkit.inventory.ItemStack bukkitItem) {
        if (bukkitItem == null) {
            return SpigotConversionUtil.fromBukkitItemStack(new org.bukkit.inventory.ItemStack(Material.AIR));
        }
        return SpigotConversionUtil.fromBukkitItemStack(bukkitItem);
    }
}

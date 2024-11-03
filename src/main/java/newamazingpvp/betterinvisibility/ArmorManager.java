package newamazingpvp.betterinvisibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorManager {

    private final ProtocolManager protocolManager;
    private final ConfigManager configManager;

    public ArmorManager(ProtocolManager protocolManager, ConfigManager configManager) {
        this.protocolManager = protocolManager;
        this.configManager = configManager;
    }

    public void restoreArmor(Player player) {
        PacketContainer restoreArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        restoreArmorPacket.getIntegers().write(0, player.getEntityId());

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        ArrayList<EnumWrappers.ItemSlot> slots = new ArrayList<>();

        if (configManager.isHideBoots()) {
            slots.add(EnumWrappers.ItemSlot.FEET);
        }
        if (configManager.isHideLeggings()) {
            slots.add(EnumWrappers.ItemSlot.LEGS);
        }
        if (configManager.isHideChestplate()) {
            slots.add(EnumWrappers.ItemSlot.CHEST);
        }
        if (configManager.isHideHelmet()) {
            slots.add(EnumWrappers.ItemSlot.HEAD);
        }
        if (configManager.isHideMainhand()) {
            slots.add(EnumWrappers.ItemSlot.MAINHAND);
        }
        if (configManager.isHideOffhand()) {
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

    public void removeAllArmor(Player player) {
        PacketContainer clearArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        clearArmorPacket.getIntegers().write(0, player.getEntityId());

        ArrayList<EnumWrappers.ItemSlot> slots = new ArrayList<>();

        if (configManager.isHideBoots()) {
            slots.add(EnumWrappers.ItemSlot.FEET);
        }
        if (configManager.isHideLeggings()) {
            slots.add(EnumWrappers.ItemSlot.LEGS);
        }
        if (configManager.isHideChestplate()) {
            slots.add(EnumWrappers.ItemSlot.CHEST);
        }
        if (configManager.isHideHelmet()) {
            slots.add(EnumWrappers.ItemSlot.HEAD);
        }
        if (configManager.isHideMainhand()) {
            slots.add(EnumWrappers.ItemSlot.MAINHAND);
        }
        if (configManager.isHideOffhand()) {
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
}
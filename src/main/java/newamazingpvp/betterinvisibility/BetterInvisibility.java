package newamazingpvp.betterinvisibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
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
            if (event.getNewEffect() != null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                removeAllArmor(player);
            }
        }
    }


    public void removeAllArmor(Player player) {
        PacketContainer clearArmorPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        clearArmorPacket.getIntegers().write(0, player.getEntityId());

        // Iterate through armor slots 2 (Chestplate), 3 (Leggings), 4 (Boots), and 5 (Helmet)
        for (int slot = 2; slot <= 5; slot++) {
            EnumWrappers.ItemSlot itemSlot = EnumWrappers.ItemSlot.values()[slot];
            ItemStack airItem = new ItemStack(Material.AIR);
            Pair<EnumWrappers.ItemSlot, ItemStack> slotItemPair = new Pair<>(itemSlot, airItem);
            clearArmorPacket.getSlotStackPairLists().write(0, Collections.singletonList(slotItemPair));
        }

        List<Player> playersInWorld = player.getWorld().getPlayers();

        for (Player currentPlayer : playersInWorld) {
            try {
                protocolManager.sendServerPacket(currentPlayer, clearArmorPacket);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}

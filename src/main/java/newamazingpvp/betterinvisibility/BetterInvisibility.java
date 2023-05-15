package newamazingpvp.betterinvisibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public final class BetterInvisibility extends JavaPlugin {

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListener();

    }

    public void onLoad() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void registerPacketListener() {
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player observer = event.getPlayer();
                        StructureModifier<Entity> entityMod = event.getPacket().getEntityModifier(observer.getWorld());
                        Entity entity = entityMod.read(0);
                        if (entity instanceof Player) {
                            Player player = (Player) entity;
                            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && !player.equals(observer)) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
        );
    }

    
}

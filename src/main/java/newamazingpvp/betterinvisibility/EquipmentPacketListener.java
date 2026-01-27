package newamazingpvp.betterinvisibility;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import org.bukkit.entity.Player;

public class EquipmentPacketListener extends PacketListenerAbstract {

    private final ArmorManager armorManager;

    public EquipmentPacketListener(ArmorManager armorManager) {
        super(PacketListenerPriority.NORMAL);
        this.armorManager = armorManager;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT) {
            return;
        }
        Player viewer = event.getPlayer();
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(event);
        if (viewer != null && viewer.getEntityId() == packet.getEntityId()) {
            return; // do not hide from self
        }
        if (armorManager.isInvisible(packet.getEntityId())) {
            armorManager.maskPacket(packet);
            event.markForReEncode(true);
        }
    }
}

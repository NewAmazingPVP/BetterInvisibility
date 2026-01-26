package newamazingpvp.betterinvisibility;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterInvisibility extends JavaPlugin {

    private ConfigManager configManager;
    private ArmorManager armorManager;

    @Override
    public void onEnable() {
        new Metrics(this, 18558);
        configManager = new ConfigManager(this);
        armorManager = new ArmorManager(configManager);
        getServer().getPluginManager().registerEvents(new EventListener(configManager, armorManager), this);
        PacketEvents.getAPI().getEventManager().registerListener(new EquipmentPacketListener(armorManager));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArmorManager getArmorManager() {
        return armorManager;
    }
}

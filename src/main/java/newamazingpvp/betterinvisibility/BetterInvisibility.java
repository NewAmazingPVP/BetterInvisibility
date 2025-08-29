package newamazingpvp.betterinvisibility;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterInvisibility extends JavaPlugin {

    private ConfigManager configManager;
    private ArmorManager armorManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        new Metrics(this, 18558);
        PacketEvents.getAPI().init();
        configManager = new ConfigManager(this);
        armorManager = new ArmorManager(configManager);
        getServer().getPluginManager().registerEvents(new EventListener(configManager, armorManager), this);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArmorManager getArmorManager() {
        return armorManager;
    }
}

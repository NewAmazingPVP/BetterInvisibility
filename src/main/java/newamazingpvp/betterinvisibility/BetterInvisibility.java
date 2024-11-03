package newamazingpvp.betterinvisibility;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterInvisibility extends JavaPlugin {

    private ProtocolManager protocolManager;
    private ConfigManager configManager;
    private ArmorManager armorManager;

    @Override
    public void onEnable() {
        new Metrics(this, 18558);
        protocolManager = ProtocolLibrary.getProtocolManager();
        configManager = new ConfigManager(this);
        armorManager = new ArmorManager(protocolManager, configManager);
        getServer().getPluginManager().registerEvents(new EventListener(configManager, armorManager), this);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArmorManager getArmorManager() {
        return armorManager;
    }
}
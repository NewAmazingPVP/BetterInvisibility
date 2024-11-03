package newamazingpvp.betterinvisibility;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        config.addDefault("hide.helmet", true);
        config.addDefault("hide.chestplate", true);
        config.addDefault("hide.leggings", true);
        config.addDefault("hide.boots", true);
        config.addDefault("hide.mainhand", true);
        config.addDefault("hide.offhand", true);
        config.addDefault("hide.potionParticles", false);
        config.addDefault("enable_workaround", false);
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public boolean isHideHelmet() {
        return config.getBoolean("hide.helmet");
    }

    public boolean isHideChestplate() {
        return config.getBoolean("hide.chestplate");
    }

    public boolean isHideLeggings() {
        return config.getBoolean("hide.leggings");
    }

    public boolean isHideBoots() {
        return config.getBoolean("hide.boots");
    }

    public boolean isHideMainhand() {
        return config.getBoolean("hide.mainhand");
    }

    public boolean isHideOffhand() {
        return config.getBoolean("hide.offhand");
    }

    public boolean isHidePotionParticles() {
        return config.getBoolean("hide.potionParticles");
    }

    public boolean isEnableWorkaround() {
        return config.getBoolean("enable_workaround");
    }
}
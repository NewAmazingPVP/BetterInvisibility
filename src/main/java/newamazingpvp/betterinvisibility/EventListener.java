package newamazingpvp.betterinvisibility;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EventListener implements Listener {

    private final ConfigManager configManager;
    private final ArmorManager armorManager;
    private boolean isEffectAddedByPlugin = false;

    public EventListener(ConfigManager configManager, ArmorManager armorManager) {
        this.configManager = configManager;
        this.armorManager = armorManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            armorManager.hidePlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (isEffectAddedByPlugin) {
            isEffectAddedByPlugin = false;
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (event.getNewEffect() != null && event.getNewEffect().getType() != null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                if (configManager.isHidePotionParticles()) {
                    PotionEffect potion = event.getNewEffect();
                    PotionEffect newEffect = new PotionEffect(PotionEffectType.INVISIBILITY, potion.getDuration(), potion.getAmplifier(), potion.isAmbient(), false);
                    //player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    event.setCancelled(true);
                    //player.getActivePotionEffects().clear();
                    isEffectAddedByPlugin = true;
                    player.addPotionEffect(newEffect);
                }
                armorManager.hidePlayer(player);
            } else if (event.getOldEffect() != null && event.getOldEffect().getType() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                armorManager.showPlayer(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        armorManager.stopTracking(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        armorManager.stopTracking(event.getPlayer());
    }
}

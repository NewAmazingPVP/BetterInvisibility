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
    private final FoliaCompatScheduler scheduler;
    private boolean isEffectAddedByPlugin = false;
    private final java.util.Map<java.util.UUID, FoliaCompatScheduler.TickTask> armorTasks = new java.util.concurrent.ConcurrentHashMap<>();

    public EventListener(ConfigManager configManager, ArmorManager armorManager) {
        this.configManager = configManager;
        this.armorManager = armorManager;
        this.scheduler = new FoliaCompatScheduler(BetterInvisibility.class);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            cancelTask(event.getPlayer());
            FoliaCompatScheduler.TickTask newTask = scheduler.runAtEntityTimer(event.getPlayer(), task -> {
                Player p = event.getPlayer();
                if (!p.isOnline()) {
                    task.cancel();
                    cancelTask(p);
                    return;
                }
                if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    armorManager.removeAllArmor(p);
                } else {
                    armorManager.restoreArmor(p);
                    task.cancel();
                    cancelTask(p);
                }
            }, 0L, 1L);
            armorTasks.put(event.getPlayer().getUniqueId(), newTask);
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
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    isEffectAddedByPlugin = true;
                    player.addPotionEffect(newEffect);
                }
                cancelTask(player);
                FoliaCompatScheduler.TickTask t = scheduler.runAtEntityTimer(player, task -> armorManager.removeAllArmor(player), 0L, 1L);
                armorTasks.put(player.getUniqueId(), t);
            } else if (event.getOldEffect() != null && event.getOldEffect().getType() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                cancelTask(player);
                FoliaCompatScheduler.TickTask t = scheduler.runAtEntityTimer(player, task -> armorManager.restoreArmor(player), 0L, 1L);
                armorTasks.put(player.getUniqueId(), t);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelTask(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        cancelTask(event.getPlayer());
    }

    private void cancelTask(Player player) {
        FoliaCompatScheduler.TickTask t = armorTasks.remove(player.getUniqueId());
        if (t != null) {
            try {
                t.cancel();
            } catch (Throwable ignored) {
            }
        }
    }
}

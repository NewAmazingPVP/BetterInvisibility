package newamazingpvp.betterinvisibility;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;

public class EventListener implements Listener {

    private final ConfigManager configManager;
    private final ArmorManager armorManager;
    private boolean isEffectAddedByPlugin = false;
    private final HashMap<UUID, Long> lastHitTimestamps = new HashMap<>();

    public EventListener(ConfigManager configManager, ArmorManager armorManager) {
        this.configManager = configManager;
        this.armorManager = armorManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        armorManager.removeAllArmor(event.getPlayer());
                    } else {
                        armorManager.restoreArmor(event.getPlayer());
                        this.cancel();
                    }
                }
            };
            runnable.runTaskTimer(Bukkit.getPluginManager().getPlugin("BetterInvisibility"), 0L, 1L);
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
                if(configManager.isHidePotionParticles()) {
                    PotionEffect potion = event.getNewEffect();
                    PotionEffect newEffect = new PotionEffect(PotionEffectType.INVISIBILITY, potion.getDuration(), potion.getAmplifier(), potion.isAmbient(), false);
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    isEffectAddedByPlugin = true;
                    player.addPotionEffect(newEffect);
                    isEffectAddedByPlugin = true;
                }
                Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("BetterInvisibility"), () -> armorManager.removeAllArmor(player), 0L, 1L);

            } else if (event.getOldEffect() != null && event.getOldEffect().getType() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("BetterInvisibility"), () -> armorManager.restoreArmor(player), 0L, 1L);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) throws InvocationTargetException {
        if(!configManager.isEnableWorkaround()){
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {

                event.setCancelled(true);

                long currentTime = System.currentTimeMillis();
                long lastHitTime = lastHitTimestamps.getOrDefault(player.getUniqueId(), 0L);
                long cooldownMillis = 500;

                if (currentTime - lastHitTime < cooldownMillis) {
                    return;
                }

                lastHitTimestamps.put(player.getUniqueId(), currentTime);

                double damage = event.getFinalDamage();

                player.setHealth(Math.max(0, player.getHealth() - damage));

                Location attackerLocation = attacker.getLocation();
                Location targetLocation = player.getLocation();

                Vector knockbackDirection = targetLocation.toVector().subtract(attackerLocation.toVector()).normalize();

                double knockbackMagnitude = attacker.isSprinting() ? 1.3 : 0.8;

                Vector horizontalKnockback = knockbackDirection.multiply(knockbackMagnitude);

                Vector verticalKnockback = new Vector(0, 0.35, 0);

                Vector knockback = horizontalKnockback.add(verticalKnockback);

                player.setVelocity(knockback);
            }
        }
    }
}
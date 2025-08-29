package newamazingpvp.betterinvisibility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class FoliaCompatScheduler {

    public interface TaskHandle {
        void cancel();
    }

    public interface TickTask extends TaskHandle {
    }

    private final Plugin plugin;

    public FoliaCompatScheduler(Class<?> pluginMainClass) {
        Plugin found = null;
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (pluginMainClass.isInstance(p)) {
                found = p;
                break;
            }
        }
        this.plugin = Objects.requireNonNull(found, "Plugin instance not found");
    }

    public TickTask runAtEntityTimer(Entity entity, Consumer<TaskHandle> runnable, long delayTicks, long periodTicks) {
        try {
            Method getScheduler = entity.getClass().getMethod("getScheduler");
            Object entityScheduler = getScheduler.invoke(entity);

            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            Method cancelMethod = scheduledTaskClass.getMethod("cancel");

            Method runAtFixedRate = entityScheduler.getClass().getMethod(
                    "runAtFixedRate",
                    Plugin.class,
                    Consumer.class,
                    long.class,
                    long.class
            );

            AtomicReference<Object> scheduledRef = new AtomicReference<>();
            Consumer<Object> foliaConsumer = new Consumer<Object>() {
                @Override
                public void accept(Object st) {
                    runnable.accept(new TaskHandle() {
                        @Override
                        public void cancel() {
                            try {
                                cancelMethod.invoke(scheduledRef.get());
                            } catch (Throwable ignored) {
                            }
                        }
                    });
                }
            };

            Object scheduled = runAtFixedRate.invoke(entityScheduler, plugin, foliaConsumer, delayTicks, periodTicks);
            scheduledRef.set(scheduled);

            return new TickTask() {
                @Override
                public void cancel() {
                    try {
                        cancelMethod.invoke(scheduledRef.get());
                    } catch (Throwable ignored) {
                    }
                }
            };
        } catch (Throwable ignored) {
        }

        java.util.concurrent.atomic.AtomicInteger taskId = new java.util.concurrent.atomic.AtomicInteger();
        taskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                runnable.accept(new TaskHandle() {
                    @Override
                    public void cancel() {
                        try {
                            Bukkit.getScheduler().cancelTask(taskId.get());
                        } catch (Throwable ignored) {
                        }
                    }
                });
            }
        }, delayTicks, periodTicks));

        return new TickTask() {
            @Override
            public void cancel() {
                Bukkit.getScheduler().cancelTask(taskId.get());
            }
        };
    }
}

package ac.reaper.platform.fabric.scheduler;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.entity.ReaperEntity;
import ac.reaper.platform.api.scheduler.EntityScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FabricEntityScheduler implements EntityScheduler {
    // TODO (Cross-platform) (Threading) try to make this not Concurrent
    private final Map<FabricPlatformScheduler.ScheduledTask, Runnable> taskMap = new ConcurrentHashMap<>();
    private final ReaperPlugin plugin;

    public FabricEntityScheduler(ReaperPlugin plugin) {
        this.plugin = plugin;
        ServerTickEvents.END_SERVER_TICK.register(this::handleTasks);
    }

    private void handleTasks(MinecraftServer server) {
        FabricPlatformScheduler.handleSyncTasks(taskMap, server, plugin);
    }

    @Override
    public void execute(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable run, @Nullable Runnable retired, long delay) {
        runDelayed(entity, plugin, run, retired, delay);
    }

    @Override
    public TaskHandle run(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired) {
        return runDelayed(entity, plugin, task, retired, 0);
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long delayTicks) {
        FabricPlatformScheduler.ScheduledTask scheduledTask = new FabricPlatformScheduler.ScheduledTask(
                () -> {
                    task.run();
                    if (retired != null && entity.isDead()) {
                        retired.run();
                    }
                },
                ReaperACFabricLoaderPlugin.FABRIC_SERVER.getTickCount() + delayTicks,
                0,
                false,
                plugin
        );
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask, true); // true for sync
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long initialDelayTicks, long periodTicks) {
        FabricPlatformScheduler.ScheduledTask scheduledTask = new FabricPlatformScheduler.ScheduledTask(
                () -> {
                    task.run();
                    if (retired != null && entity.isDead()) {
                        retired.run();
                    }
                },
                ReaperACFabricLoaderPlugin.FABRIC_SERVER.getTickCount() + initialDelayTicks,
                periodTicks,
                true,
                plugin
        );
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask, true); // true for sync
    }

    public void cancel(@NotNull ReaperPlugin plugin) {
        FabricPlatformScheduler.cancelPluginTasks(taskMap, plugin);
    }

    public void cancelAll() {
        FabricPlatformScheduler.cancelAllTasks(taskMap);
    }
}

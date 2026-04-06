package ac.reaper.reaperac.platform.fabric.scheduler;

import ac.reaper.reaperac.api.plugin.ReaperPlugin;
import ac.reaper.reaperac.platform.api.scheduler.GlobalRegionScheduler;
import ac.reaper.reaperac.platform.api.scheduler.TaskHandle;
import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FabricGlobalRegionScheduler implements GlobalRegionScheduler {
    // TODO (Cross-platform) (Threading) try to make this not Concurrent
    private final Map<FabricPlatformScheduler.ScheduledTask, Runnable> taskMap = new ConcurrentHashMap<>();
    private final ReaperPlugin plugin;

    public FabricGlobalRegionScheduler(ReaperPlugin plugin) {
        this.plugin = plugin;
        // Register the task handler to run on server tick
        ServerTickEvents.END_SERVER_TICK.register(this::handleTasks);
    }

    private void handleTasks(MinecraftServer server) {
        FabricPlatformScheduler.handleSyncTasks(taskMap, server, plugin);
    }

    @Override
    public void execute(@NotNull ReaperPlugin plugin, @NotNull Runnable run) {
        run(plugin, run);
    }

    @Override
    public TaskHandle run(@NotNull ReaperPlugin plugin, @NotNull Runnable task) {
        return runDelayed(plugin, task, 0);
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long delay) {
        FabricPlatformScheduler.ScheduledTask scheduledTask = new FabricPlatformScheduler.ScheduledTask(
                task,
                GrimACFabricLoaderPlugin.FABRIC_SERVER.getTickCount() + delay,
                0,
                false,
                plugin
        );
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask, true); // true for sync
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        FabricPlatformScheduler.ScheduledTask scheduledTask = new FabricPlatformScheduler.ScheduledTask(
                task,
                GrimACFabricLoaderPlugin.FABRIC_SERVER.getTickCount() + initialDelayTicks,
                periodTicks,
                true,
                plugin
        );
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask, true); // true for sync
    }

    @Override
    public void cancel(@NotNull ReaperPlugin plugin) {
        FabricPlatformScheduler.cancelPluginTasks(taskMap, plugin);
    }

    // New method to cancel all tasks
    public void cancelAll() {
        FabricPlatformScheduler.cancelAllTasks(taskMap);
    }
}

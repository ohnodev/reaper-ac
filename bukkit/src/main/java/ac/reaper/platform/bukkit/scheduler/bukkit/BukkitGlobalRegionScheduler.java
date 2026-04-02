package ac.reaper.platform.bukkit.scheduler.bukkit;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.scheduler.GlobalRegionScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class BukkitGlobalRegionScheduler implements GlobalRegionScheduler {

    private final BukkitScheduler bukkitScheduler = Bukkit.getScheduler();

    @Override
    public void execute(@NotNull ReaperPlugin plugin, @NotNull Runnable task) {
        bukkitScheduler.runTask(ReaperACBukkitLoaderPlugin.LOADER, task);
    }

    @Override
    public TaskHandle run(@NotNull ReaperPlugin plugin, @NotNull Runnable task) {
        return new BukkitTaskHandle(bukkitScheduler.runTask(ReaperACBukkitLoaderPlugin.LOADER, task));
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long delay) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskLater(ReaperACBukkitLoaderPlugin.LOADER, task, delay));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskTimer(ReaperACBukkitLoaderPlugin.LOADER, task, initialDelayTicks, periodTicks));
    }

    @Override
    public void cancel(@NotNull ReaperPlugin plugin) {
        bukkitScheduler.cancelTasks(ReaperACBukkitLoaderPlugin.LOADER);
    }
}

package ac.reaper.platform.bukkit.scheduler.bukkit;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.scheduler.AsyncScheduler;
import ac.reaper.platform.api.scheduler.PlatformScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BukkitAsyncScheduler implements AsyncScheduler {

    private final BukkitScheduler bukkitScheduler = Bukkit.getScheduler();

    @Override
    public TaskHandle runNow(@NotNull ReaperPlugin plugin, @NotNull Runnable task) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskAsynchronously(ReaperACBukkitLoaderPlugin.LOADER, task));
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long delay, @NotNull TimeUnit timeUnit) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskLaterAsynchronously(
                ReaperACBukkitLoaderPlugin.LOADER,
                task,
                PlatformScheduler.convertTimeToTicks(delay, timeUnit)
        ));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long delay, long period, @NotNull TimeUnit timeUnit) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskTimerAsynchronously(
                ReaperACBukkitLoaderPlugin.LOADER,
                task,
                PlatformScheduler.convertTimeToTicks(delay, timeUnit),
                PlatformScheduler.convertTimeToTicks(period, timeUnit)
        ));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskTimerAsynchronously(
                ReaperACBukkitLoaderPlugin.LOADER,
                task,
                initialDelayTicks,
                periodTicks
        ));
    }

    @Override
    public void cancel(@NotNull ReaperPlugin plugin) {
        bukkitScheduler.cancelTasks(ReaperACBukkitLoaderPlugin.LOADER);
    }
}

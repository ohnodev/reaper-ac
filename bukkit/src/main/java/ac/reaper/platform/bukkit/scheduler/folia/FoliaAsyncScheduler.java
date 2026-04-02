package ac.reaper.platform.bukkit.scheduler.folia;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.scheduler.AsyncScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class FoliaAsyncScheduler implements AsyncScheduler {

    private final io.papermc.paper.threadedregions.scheduler.AsyncScheduler scheduler = Bukkit.getAsyncScheduler();

    @Override
    public TaskHandle runNow(@NotNull ReaperPlugin plugin, @NotNull Runnable task) {
        return new FoliaTaskHandle(scheduler.runNow(ReaperACBukkitLoaderPlugin.LOADER, ignored -> task.run()));
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long delay, @NotNull TimeUnit timeUnit) {
        return new FoliaTaskHandle(scheduler.runDelayed(
                ReaperACBukkitLoaderPlugin.LOADER,
                ignored -> task.run(),
                delay,
                timeUnit
        ));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long delay, long period, @NotNull TimeUnit timeUnit) {
        return new FoliaTaskHandle(scheduler.runAtFixedRate(
                ReaperACBukkitLoaderPlugin.LOADER,
                ignored -> task.run(),
                delay,
                period,
                timeUnit
        ));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return new FoliaTaskHandle(scheduler.runAtFixedRate(
                ReaperACBukkitLoaderPlugin.LOADER,
                ignored -> task.run(),
                initialDelayTicks * 50,
                periodTicks * 50,
                TimeUnit.MILLISECONDS
        ));
    }

    @Override
    public void cancel(@NotNull ReaperPlugin plugin) {
        scheduler.cancelTasks(ReaperACBukkitLoaderPlugin.LOADER);
    }
}

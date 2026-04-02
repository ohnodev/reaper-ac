package ac.reaper.platform.bukkit.scheduler.folia;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.scheduler.GlobalRegionScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class FoliaGlobalRegionScheduler implements GlobalRegionScheduler {

    private final io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler globalRegionScheduler = Bukkit.getGlobalRegionScheduler();

    @Override
    public void execute(@NotNull ReaperPlugin plugin, @NotNull Runnable task) {
        globalRegionScheduler.execute(ReaperACBukkitLoaderPlugin.LOADER, task);
    }

    @Override
    public TaskHandle run(@NotNull ReaperPlugin plugin, @NotNull Runnable task) {
        return new FoliaTaskHandle(globalRegionScheduler.run(ReaperACBukkitLoaderPlugin.LOADER, ignored -> task.run()));
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long delay) {
        return new FoliaTaskHandle(globalRegionScheduler.runDelayed(ReaperACBukkitLoaderPlugin.LOADER, ignored -> task.run(), delay));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return new FoliaTaskHandle(globalRegionScheduler.runAtFixedRate(ReaperACBukkitLoaderPlugin.LOADER, ignored -> task.run(), initialDelayTicks, periodTicks));
    }

    @Override
    public void cancel(@NotNull ReaperPlugin plugin) {
        globalRegionScheduler.cancelTasks(ReaperACBukkitLoaderPlugin.LOADER);
    }
}

package ac.reaper.platform.bukkit.scheduler.bukkit;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.entity.ReaperEntity;
import ac.reaper.platform.api.scheduler.EntityScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitEntityScheduler implements EntityScheduler {
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    @Override
    public void execute(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable run, @Nullable Runnable retired, long delay) {
        scheduler.runTaskLater(ReaperACBukkitLoaderPlugin.LOADER, run, delay);
    }

    @Override
    public TaskHandle run(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired) {
        return new BukkitTaskHandle(scheduler.runTask(ReaperACBukkitLoaderPlugin.LOADER, task));
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long delayTicks) {
        return new BukkitTaskHandle(scheduler.runTaskLater(ReaperACBukkitLoaderPlugin.LOADER, task, delayTicks));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long initialDelayTicks, long periodTicks) {
        return new BukkitTaskHandle(scheduler.runTaskTimer(ReaperACBukkitLoaderPlugin.LOADER, task, initialDelayTicks, periodTicks));
    }
}

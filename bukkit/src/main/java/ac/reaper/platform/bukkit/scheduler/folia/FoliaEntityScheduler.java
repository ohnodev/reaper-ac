package ac.reaper.platform.bukkit.scheduler.folia;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.entity.ReaperEntity;
import ac.reaper.platform.api.scheduler.EntityScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import ac.reaper.platform.bukkit.entity.BukkitReaperEntity;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FoliaEntityScheduler implements EntityScheduler {

    @Override
    public void execute(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long delay) {
        ((BukkitReaperEntity) entity).getBukkitEntity().getScheduler().execute(ReaperACBukkitLoaderPlugin.LOADER, task, retired, delay);
    }

    @Override
    public TaskHandle run(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired) {
        ScheduledTask scheduled = ((BukkitReaperEntity) entity).getBukkitEntity().getScheduler().run(
                ReaperACBukkitLoaderPlugin.LOADER,
                ignored -> task.run(),
                retired
        );

        return scheduled == null ? null : new FoliaTaskHandle(scheduled);
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long delayTicks) {
        ScheduledTask scheduled = ((BukkitReaperEntity) entity).getBukkitEntity().getScheduler().runDelayed(
                ReaperACBukkitLoaderPlugin.LOADER,
                ignored -> task.run(),
                retired,
                delayTicks
        );

        return scheduled == null ? null : new FoliaTaskHandle(scheduled);
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperEntity entity, @NotNull ReaperPlugin plugin, @NotNull Runnable task, @Nullable Runnable retired, long initialDelayTicks, long periodTicks) {
        ScheduledTask scheduled = ((BukkitReaperEntity) entity).getBukkitEntity().getScheduler().runAtFixedRate(
                ReaperACBukkitLoaderPlugin.LOADER,
                ignored -> task.run(),
                retired,
                initialDelayTicks,
                periodTicks
        );

        return scheduled == null ? null : new FoliaTaskHandle(scheduled);
    }
}

package ac.reaper.platform.bukkit.scheduler.bukkit;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.scheduler.RegionScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.api.world.PlatformWorld;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import ac.reaper.utils.math.Location;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class BukkitRegionScheduler implements RegionScheduler {

    private final BukkitScheduler bukkitScheduler = Bukkit.getScheduler();

    @Override
    public void execute(@NotNull ReaperPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task) {
        bukkitScheduler.runTask(ReaperACBukkitLoaderPlugin.LOADER, task);
    }

    @Override
    public void execute(@NotNull ReaperPlugin plugin, @NotNull Location location, @NotNull Runnable task) {
        bukkitScheduler.runTask(ReaperACBukkitLoaderPlugin.LOADER, task);
    }

    @Override
    public TaskHandle run(@NotNull ReaperPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task) {
        return new BukkitTaskHandle(bukkitScheduler.runTask(ReaperACBukkitLoaderPlugin.LOADER, task));
    }

    @Override
    public TaskHandle run(@NotNull ReaperPlugin plugin, @NotNull Location location, @NotNull Runnable task) {
        return new BukkitTaskHandle(bukkitScheduler.runTask(ReaperACBukkitLoaderPlugin.LOADER, task));
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task, long delayTicks) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskLater(ReaperACBukkitLoaderPlugin.LOADER, task, delayTicks));
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull Location location, @NotNull Runnable task, long delayTicks) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskLater(ReaperACBukkitLoaderPlugin.LOADER, task, delayTicks));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskTimer(ReaperACBukkitLoaderPlugin.LOADER, task, initialDelayTicks, periodTicks));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Location location, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return new BukkitTaskHandle(bukkitScheduler.runTaskTimer(ReaperACBukkitLoaderPlugin.LOADER, task, initialDelayTicks, periodTicks));
    }
}

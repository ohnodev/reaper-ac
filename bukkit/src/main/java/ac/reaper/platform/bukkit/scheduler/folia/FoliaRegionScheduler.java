package ac.reaper.platform.bukkit.scheduler.folia;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.scheduler.RegionScheduler;
import ac.reaper.platform.api.scheduler.TaskHandle;
import ac.reaper.platform.api.world.PlatformWorld;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import ac.reaper.platform.bukkit.world.BukkitPlatformWorld;
import ac.reaper.utils.math.Location;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class FoliaRegionScheduler implements RegionScheduler {

    private final io.papermc.paper.threadedregions.scheduler.RegionScheduler regionScheduler = Bukkit.getRegionScheduler();

    @Override
    public void execute(@NotNull ReaperPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task) {
        regionScheduler.execute(ReaperACBukkitLoaderPlugin.LOADER, ((BukkitPlatformWorld) world).getBukkitWorld(), chunkX, chunkZ, task);
    }

    @Override
    public void execute(@NotNull ReaperPlugin plugin, @NotNull Location location, @NotNull Runnable task) {
        execute(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task);
    }

    @Override
    public TaskHandle run(@NotNull ReaperPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task) {
        return new FoliaTaskHandle(regionScheduler.run(
                ReaperACBukkitLoaderPlugin.LOADER,
                ((BukkitPlatformWorld) world).getBukkitWorld(),
                chunkX,
                chunkZ,
                ignored -> task.run()
        ));
    }

    @Override
    public TaskHandle run(@NotNull ReaperPlugin plugin, @NotNull Location location, @NotNull Runnable task) {
        return run(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task);
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task, long delayTicks) {
        return new FoliaTaskHandle(regionScheduler.runDelayed(
                ReaperACBukkitLoaderPlugin.LOADER,
                ((BukkitPlatformWorld) world).getBukkitWorld(),
                chunkX,
                chunkZ,
                ignored -> task.run(),
                delayTicks
        ));
    }

    @Override
    public TaskHandle runDelayed(@NotNull ReaperPlugin plugin, @NotNull Location location, @NotNull Runnable task, long delayTicks) {
        return runDelayed(plugin, location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task, delayTicks);
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull PlatformWorld world, int chunkX, int chunkZ, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return new FoliaTaskHandle(regionScheduler.runAtFixedRate(
                ReaperACBukkitLoaderPlugin.LOADER,
                ((BukkitPlatformWorld) world).getBukkitWorld(),
                chunkX,
                chunkZ,
                ignored -> task.run(),
                initialDelayTicks,
                periodTicks
        ));
    }

    @Override
    public TaskHandle runAtFixedRate(@NotNull ReaperPlugin plugin, @NotNull Location location, @NotNull Runnable task, long initialDelayTicks, long periodTicks) {
        return runAtFixedRate(
                plugin,
                location.getWorld(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4,
                task,
                initialDelayTicks,
                periodTicks
        );
    }
}

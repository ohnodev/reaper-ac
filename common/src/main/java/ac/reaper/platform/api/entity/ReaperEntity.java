package ac.reaper.platform.api.entity;

import ac.reaper.api.ReaperIdentity;
import ac.reaper.platform.api.world.PlatformWorld;
import ac.reaper.utils.math.Location;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface ReaperEntity extends ReaperIdentity {
    /**
     * Eject any passenger.
     *
     * @return True if there was a passenger.
     */
    boolean eject();

    CompletableFuture<Boolean> teleportAsync(Location location);

    @NotNull
    Object getNative();

    boolean isDead();

    PlatformWorld getWorld();

    Location getLocation();

    double distanceSquared(double x, double y, double z);
}

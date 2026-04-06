package ac.reaper.reaperac.platform.api.entity;

import ac.reaper.reaperac.api.ReaperIdentity;
import ac.reaper.reaperac.platform.api.world.PlatformWorld;
import ac.reaper.reaperac.utils.math.Location;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface GrimEntity extends ReaperIdentity {
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

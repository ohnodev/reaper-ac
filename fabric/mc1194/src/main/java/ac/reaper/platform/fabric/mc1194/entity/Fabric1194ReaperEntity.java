package ac.reaper.platform.fabric.mc1194.entity;

import ac.reaper.platform.fabric.mc1171.entity.Fabric1170ReaperEntity;
import ac.reaper.platform.fabric.utils.thread.FabricFutureUtil;
import ac.reaper.utils.math.Location;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;

public class Fabric1194ReaperEntity extends Fabric1170ReaperEntity {

    public Fabric1194ReaperEntity(Entity entity) {
        super(entity);
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Location location) {
        return FabricFutureUtil.supplySync(() -> {
            if (entity.getLevel() instanceof ServerLevel) {
                entity.teleportTo(
                        (ServerLevel) location.getWorld(),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        EnumSet.noneOf(RelativeMovement.class), // todo change to match paper? Do they do this?
                        location.getYaw(),
                        location.getPitch()

                );
                return true;
            }
            return false;
        });
    }
}

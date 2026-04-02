package ac.reaper.platform.fabric.mc1161.entity;

import ac.reaper.platform.fabric.entity.AbstractFabricReaperEntity;
import ac.reaper.platform.fabric.utils.thread.FabricFutureUtil;
import ac.reaper.utils.math.Location;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class Fabric1161ReaperEntity extends AbstractFabricReaperEntity {

    public Fabric1161ReaperEntity(Entity entity) {
        super(entity);
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Location location) {
        return FabricFutureUtil.supplySync(() -> {
            if (entity.getCommandSenderWorld() instanceof ServerLevel) {
                entity.teleportToWithTicket(
                        location.getX(),
                        location.getY(),
                        location.getZ()
                );
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean isDead() {
        return entity instanceof LivingEntity living ? living.isDeadOrDying() : entity.removed;
    }
}

package ac.reaper.reaperac.platform.fabric.mc262.entity;

import ac.reaper.reaperac.platform.api.world.PlatformWorld;
import ac.reaper.reaperac.platform.fabric.entity.AbstractFabricGrimEntity;
import ac.reaper.reaperac.platform.fabric.utils.thread.FabricFutureUtil;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.math.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

public class Fabric262GrimEntity extends AbstractFabricGrimEntity {
    public Fabric262GrimEntity(Entity entity) {
        super(entity);
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Location location) {
        return FabricFutureUtil.supplySync(() -> {
            if (!(entity.level() instanceof ServerLevel)) {
                return false;
            }
            PlatformWorld world = location.getWorld();
            if (world == null || !(world instanceof ServerLevel targetLevel)) {
                LogUtil.info("teleportAsync skipped: location world missing or not a ServerLevel");
                return false;
            }
            entity.teleportTo(
                    targetLevel,
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    EnumSet.noneOf(Relative.class),
                    location.getYaw(),
                    location.getPitch(),
                    false
            );
            return true;
        });
    }

    @Override
    public boolean isDead() {
        return entity instanceof LivingEntity living ? living.isDeadOrDying() : entity.isRemoved();
    }
}

package ac.reaper.platform.fabric.mc1161.player;

import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.api.world.PlatformWorld;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.player.AbstractFabricPlatformPlayer;
import ac.reaper.platform.fabric.utils.thread.FabricFutureUtil;
import ac.reaper.utils.anticheat.LogUtil;
import ac.reaper.utils.math.Location;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;

public class Fabric1161PlatformPlayer extends AbstractFabricPlatformPlayer {
    public Fabric1161PlatformPlayer(ServerPlayer player) {
        super(player);
    }

    @Override
    public Sender getSender() {
        return ReaperACFabricLoaderPlugin.LOADER.getFabricSenderFactory().wrap(fabricPlayer.createCommandSourceStack());
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Location location) {
        if (location == null) {
            return CompletableFuture.completedFuture(false);
        }
        PlatformWorld world = location.getWorld();
        if (world == null || !(world instanceof ServerLevel targetLevel)) {
            return CompletableFuture.completedFuture(false);
        }
        final double targetX = location.getX();
        final double targetY = location.getY();
        final double targetZ = location.getZ();
        final float targetYaw = location.getYaw();
        final float targetPitch = location.getPitch();
        return FabricFutureUtil.supplySync(() -> {
            try {
                fabricPlayer.teleportTo(
                        targetLevel,
                        targetX,
                        targetY,
                        targetZ,
                        EnumSet.noneOf(Relative.class),
                        targetYaw,
                        targetPitch,
                        false
                );
                if (fabricPlayer.level() != targetLevel) {
                    return false;
                }
                double epsilon = 1e-3;
                float yawDelta = wrappedAngleDelta(fabricPlayer.getYRot(), targetYaw);
                if (Math.abs(fabricPlayer.getX() - targetX) > epsilon
                        || Math.abs(fabricPlayer.getY() - targetY) > epsilon
                        || Math.abs(fabricPlayer.getZ() - targetZ) > epsilon
                        || yawDelta > 0.01f
                        || Math.abs(fabricPlayer.getXRot() - targetPitch) > 0.01f) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                LogUtil.warn("teleportAsync failed for " + fabricPlayer.getScoreboardName(), e);
                return false;
            }
        });
    }

    private static float wrappedAngleDelta(float a, float b) {
        return Math.abs(((a - b + 540.0f) % 360.0f) - 180.0f);
    }
}

package ac.reaper.reaperac.platform.fabric.mc262.player;

import ac.reaper.reaperac.platform.api.sender.Sender;
import ac.reaper.reaperac.platform.api.world.PlatformWorld;
import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.reaper.reaperac.platform.fabric.player.AbstractFabricPlatformPlayer;
import ac.reaper.reaperac.platform.fabric.utils.thread.FabricFutureUtil;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.math.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

public class Fabric262PlatformPlayer extends AbstractFabricPlatformPlayer {
    public Fabric262PlatformPlayer(ServerPlayer player) {
        super(player);
    }

    @Override
    public Sender getSender() {
        return GrimACFabricLoaderPlugin.LOADER.getFabricSenderFactory().wrap(fabricPlayer.createCommandSourceStack());
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Location location) {
        PlatformWorld world = location.getWorld();
        if (world == null || !(world instanceof ServerLevel targetLevel)) {
            return CompletableFuture.completedFuture(false);
        }
        return FabricFutureUtil.supplySync(() -> {
            try {
                fabricPlayer.teleportTo(
                        targetLevel,
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        EnumSet.noneOf(Relative.class),
                        location.getYaw(),
                        location.getPitch(),
                        true
                );
                if (fabricPlayer.level() != targetLevel) {
                    return false;
                }
                double epsilon = 1e-3;
                float yawDelta = wrappedAngleDelta(fabricPlayer.getYRot(), location.getYaw());
                return Math.abs(fabricPlayer.getX() - location.getX()) <= epsilon
                        && Math.abs(fabricPlayer.getY() - location.getY()) <= epsilon
                        && Math.abs(fabricPlayer.getZ() - location.getZ()) <= epsilon
                        && yawDelta <= 0.01f
                        && Math.abs(fabricPlayer.getXRot() - location.getPitch()) <= 0.01f;
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

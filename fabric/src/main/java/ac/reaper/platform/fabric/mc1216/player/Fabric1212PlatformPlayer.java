package ac.reaper.platform.fabric.mc1216.player;

import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.api.world.PlatformWorld;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.mc1205.player.Fabric1202PlatformPlayer;
import ac.reaper.platform.fabric.utils.thread.FabricFutureUtil;
import ac.reaper.utils.anticheat.LogUtil;
import ac.reaper.utils.math.Location;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;

public class Fabric1212PlatformPlayer extends Fabric1202PlatformPlayer {
    public Fabric1212PlatformPlayer(ServerPlayer player) {
        super(player);
    }

    @Override
    public Sender getSender() {
        return ReaperACFabricLoaderPlugin.LOADER.getFabricSenderFactory().wrap(fabricPlayer.createCommandSourceStack());
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Location location) {
        PlatformWorld world = location.getWorld();
        if (world == null || !(world instanceof ServerLevel targetLevel)) {
            return CompletableFuture.completedFuture(false);
        }
        return FabricFutureUtil.supplySync(() -> {
            try {
                // MC 1.21.2+: last boolean is the overload that matches server teleport API; differs from
                // Fabric1161PlatformPlayer (false) due to signature/behavior on older branches.
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

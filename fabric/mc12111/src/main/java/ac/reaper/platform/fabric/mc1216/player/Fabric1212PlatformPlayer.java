package ac.reaper.platform.fabric.mc1216.player;

import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.mc1205.player.Fabric1202PlatformPlayer;
import ac.reaper.platform.fabric.utils.thread.FabricFutureUtil;
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
        return FabricFutureUtil.supplySync(() -> {
            fabricPlayer.teleportTo(
                    (ServerLevel) location.getWorld(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    EnumSet.noneOf(Relative.class), // todo change to match paper? Do they do this?
                    location.getYaw(),
                    location.getPitch(),
                    true
            );
            return true;
        });
    }
}

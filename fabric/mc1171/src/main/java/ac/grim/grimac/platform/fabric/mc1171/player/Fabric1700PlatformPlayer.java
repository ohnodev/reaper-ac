package ac.grim.grimac.platform.fabric.mc1171.player;

import ac.grim.grimac.platform.fabric.mc1611.player.Fabric1161PlatformPlayer;
import ac.grim.grimac.platform.fabric.utils.convert.FabricConversionUtil;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import net.minecraft.server.network.ServerPlayerEntity;


public class Fabric1700PlatformPlayer extends Fabric1161PlatformPlayer {
    public Fabric1700PlatformPlayer(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        fabricPlayer.changeGameMode(FabricConversionUtil.toFabricGameMode(gameMode));
    }
}

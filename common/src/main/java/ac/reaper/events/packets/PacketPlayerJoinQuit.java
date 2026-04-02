package ac.reaper.events.packets;

import ac.reaper.ReaperAPI;
import ac.reaper.platform.api.player.PlatformPlayer;
import ac.reaper.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class PacketPlayerJoinQuit extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
            // Do this after send to avoid sending packets before the PLAY state
            event.getTasksAfterSend().add(() -> ReaperAPI.INSTANCE.getPlayerDataManager().addUser(event.getUser()));
        }
    }

    @Override
    public void onUserConnect(UserConnectEvent event) {
        // Player connected too soon, perhaps late bind is off
        // Don't kick everyone on reload
        if (event.getUser().getConnectionState() == ConnectionState.PLAY && !ReaperAPI.INSTANCE.getPlayerDataManager().exemptUsers.contains(event.getUser())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onUserLogin(UserLoginEvent event) {
        Object nativePlayerObject = Objects.requireNonNull(event.getPlayer());

        // This will never throw a NPE because code is run in OnUserConnect -> onPacketSend -> OnUserLogin order
        // And the user will be added to the map before the getPlayer() method call
        @NotNull PlatformPlayer platformPlayer = ReaperAPI.INSTANCE.getPlatformPlayerFactory().getFromNativePlayerType(nativePlayerObject);

        if (ReaperAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("debug-pipeline-on-join", false)) {
            LogUtil.info("Pipeline: " + ChannelHelper.pipelineHandlerNamesAsString(event.getUser().getChannel()));
        }
        if (platformPlayer.hasPermission("reaper.alerts.enable-on-join") && platformPlayer.hasPermission("reaper.alerts")) {
            ReaperAPI.INSTANCE.getAlertManager().toggleAlerts(platformPlayer, platformPlayer.hasPermission("reaper.alerts.enable-on-join.silent"));
        }
        if (platformPlayer.hasPermission("reaper.verbose.enable-on-join") && platformPlayer.hasPermission("reaper.verbose")) {
            ReaperAPI.INSTANCE.getAlertManager().toggleVerbose(platformPlayer, platformPlayer.hasPermission("reaper.verbose.enable-on-join.silent"));
        }
        if (platformPlayer.hasPermission("reaper.brand.enable-on-join") && platformPlayer.hasPermission("reaper.brand")) {
            ReaperAPI.INSTANCE.getAlertManager().toggleBrands(platformPlayer, platformPlayer.hasPermission("reaper.brand.enable-on-join.silent"));
        }
        if (platformPlayer.hasPermission("reaper.spectate") && ReaperAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("spectators.hide-regardless", false)) {
            ReaperAPI.INSTANCE.getSpectateManager().onLogin(platformPlayer.getUniqueId());
        }
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        ReaperAPI.INSTANCE.getPlayerDataManager().onDisconnect(event.getUser());
    }
}

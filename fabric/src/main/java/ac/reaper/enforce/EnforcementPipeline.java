package ac.reaper.enforce;

import ac.reaper.bridge.RustBridge;
import ac.reaper.capture.PlayerState;
import ac.reaper.config.ReaperConfig;
import ac.reaper.schema.ActionResponse;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/**
 * Applies enforcement actions on the server thread based on Rust engine responses.
 * Called at end-of-tick after capture, ensuring all mutations happen on the main thread.
 *
 * Graceful degradation: if the bridge is disconnected or rustEngineRequired is false,
 * this pipeline becomes a no-op.
 */
public final class EnforcementPipeline {

    private static final Logger LOG = LoggerFactory.getLogger("ReaperAC-Enforce");

    private final RustBridge bridge;
    private final Map<UUID, PlayerState> playerStates;

    public EnforcementPipeline(RustBridge bridge, Map<UUID, PlayerState> playerStates) {
        this.bridge = bridge;
        this.playerStates = playerStates;
    }

    /**
     * Drain pending responses from the bridge and apply actions.
     * Must be called from the server thread only.
     */
    public void processTick(MinecraftServer server) {
        if (!bridge.isConnected() && !ReaperConfig.rustEngineRequired) {
            return;
        }

        var responses = bridge.drainResponses();
        if (responses.isEmpty()) return;

        var playerList = server.getPlayerList();

        for (var resp : responses) {
            if (resp.action == ActionResponse.Action.NONE) continue;

            UUID playerId = new UUID(resp.playerIdMsb, resp.playerIdLsb);
            ServerPlayer player = playerList.getPlayer(playerId);
            if (player == null) continue;

            switch (resp.action) {
                case FLAG -> handleFlag(player, resp);
                case SETBACK -> handleSetback(player, resp);
                case KICK -> handleKick(player, resp);
            }
        }
    }

    private void handleFlag(ServerPlayer player, ActionResponse resp) {
        LOG.info("[FLAG] {} risk={} conf={} reason={}",
                player.getGameProfile().name(),
                resp.riskScore, resp.confidence, resp.reasonCode);
    }

    private void handleSetback(ServerPlayer player, ActionResponse resp) {
        LOG.warn("[SETBACK] {} risk={} conf={} reason={}",
                player.getGameProfile().name(),
                resp.riskScore, resp.confidence, resp.reasonCode);

        player.teleportTo(
                player.level(),
                player.getX(), player.getY(), player.getZ(),
                java.util.Set.of(),
                player.getYRot(), player.getXRot(),
                false
        );

        var state = playerStates.get(player.getUUID());
        if (state != null) state.markTeleport();
    }

    private void handleKick(ServerPlayer player, ActionResponse resp) {
        LOG.error("[KICK] {} risk={} conf={} reason={}",
                player.getGameProfile().name(),
                resp.riskScore, resp.confidence, resp.reasonCode);

        player.connection.disconnect(
                Component.literal("[ReaperAC] Removed for suspicious activity (code: " + resp.reasonCode + ")")
        );
    }
}

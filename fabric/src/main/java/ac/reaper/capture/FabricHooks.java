package ac.reaper.capture;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers Fabric API event callbacks that feed {@link PlayerState} accumulators
 * with minimal per-event work (field copy only, no allocation in the hot path).
 */
public final class FabricHooks {

    private static final Logger LOG = LoggerFactory.getLogger("ReaperAC");

    private final Map<UUID, PlayerState> states = new ConcurrentHashMap<>();
    private final TickSnapshotBuffer buffer;
    private int tickCounter;

    public FabricHooks(TickSnapshotBuffer buffer) {
        this.buffer = buffer;
    }

    public Map<UUID, PlayerState> states() {
        return states;
    }

    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            UUID id = handler.getPlayer().getUUID();
            states.computeIfAbsent(id, PlayerState::new);
            LOG.debug("Tracking player {}", id);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUUID();
            states.remove(id);
            LOG.debug("Untracking player {}", id);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer) {
                var state = states.get(player.getUUID());
                if (state != null) state.onAttack();
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer) {
                var state = states.get(player.getUUID());
                if (state != null) state.onBlockUse();
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer) {
                var state = states.get(player.getUUID());
                if (state != null) state.onItemUse();
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer) {
                var ps = states.get(player.getUUID());
                if (ps != null) ps.onDig();
            }
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(this::onEndTick);

        LOG.info("ReaperAC capture hooks registered");
    }

    private void onEndTick(MinecraftServer server) {
        tickCounter++;
        var players = server.getPlayerList().getPlayers();
        for (var player : players) {
            var state = states.get(player.getUUID());
            if (state == null) continue;

            var snap = state.capture(player, tickCounter);
            buffer.offer(snap);
            state.postCapture();
        }
    }
}

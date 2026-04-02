package ac.reaper.capture;

import ac.reaper.config.ReaperConfig;
import ac.reaper.schema.PlayerTickSnapshot;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Mutable per-player accumulator that lives for the player's session.
 * Updated from Fabric event callbacks (server thread only) and flushed
 * into a {@link PlayerTickSnapshot} once per tick.
 *
 * All fields are written/read exclusively on the server thread, so no
 * synchronization is required.
 */
public final class PlayerState {

    private final UUID playerId;
    private final PlayerTickSnapshot snapshot = new PlayerTickSnapshot();

    private double prevX, prevY, prevZ;
    private int teleportGraceTicks;
    private int knockbackGraceTicks;
    private int lastSeenTick = -1;

    public PlayerState(UUID playerId) {
        this.playerId = playerId;
        snapshot.setPlayerId(playerId);
    }

    public UUID playerId() {
        return playerId;
    }

    /**
     * Called once per server tick to capture the player's current state
     * and produce an immutable snapshot for the buffer.
     */
    public PlayerTickSnapshot capture(ServerPlayer player, int currentTick) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        snapshot.tick = currentTick;
        snapshot.posX = x;
        snapshot.posY = y;
        snapshot.posZ = z;
        snapshot.deltaX = x - prevX;
        snapshot.deltaY = y - prevY;
        snapshot.deltaZ = z - prevZ;

        var vel = player.getDeltaMovement();
        snapshot.velX = vel.x;
        snapshot.velY = vel.y;
        snapshot.velZ = vel.z;

        snapshot.onGround = player.onGround();
        snapshot.inVehicle = player.isPassenger();
        snapshot.inLiquid = player.isInWater() || player.isInLava();
        snapshot.sprinting = player.isSprinting();
        snapshot.sneaking = player.isShiftKeyDown();

        if (teleportGraceTicks > 0) {
            snapshot.recentTeleport = true;
            teleportGraceTicks--;
        }
        if (knockbackGraceTicks > 0) {
            snapshot.recentKnockback = true;
            knockbackGraceTicks--;
        }

        snapshot.skippedTicks = (lastSeenTick >= 0) ? Math.max(0, currentTick - lastSeenTick - 1) : 0;
        lastSeenTick = currentTick;

        snapshot.yaw = player.getYRot();
        snapshot.pitch = player.getXRot();

        snapshot.pingBucket = clampPing(player);

        prevX = x;
        prevY = y;
        prevZ = z;

        return snapshot;
    }

    /** Call after capture() returns to zero the per-tick counters. */
    public void postCapture() {
        snapshot.resetCounters();
    }

    public void onAttack()   { snapshot.attackCount++; }
    public void onBlockUse() { snapshot.blockUseCount++; }
    public void onItemUse()  { snapshot.itemUseCount++; }
    public void onDig()      { snapshot.digActionCount++; }

    public void markTeleport() {
        teleportGraceTicks = ReaperConfig.TELEPORT_GRACE_TICKS;
    }

    public void markKnockback() {
        knockbackGraceTicks = ReaperConfig.KNOCKBACK_GRACE_TICKS;
    }

    private static int clampPing(ServerPlayer player) {
        int latency = player.connection.latency();
        return Math.clamp(latency / 10, 0, 0xFFFF);
    }
}

package ac.reaper.schema;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Fixed-layout binary frame sent from Java capture to Rust scoring engine.
 *
 * Layout (little-endian, 128 bytes fixed):
 *   [0..1]   u16  schemaVersion
 *   [2..5]   i32  tick
 *   [6..13]  u64  playerIdMsb
 *   [14..21] u64  playerIdLsb
 *   [22..29] f64  posX
 *   [30..37] f64  posY
 *   [38..45] f64  posZ
 *   [46..53] f64  deltaX
 *   [54..61] f64  deltaY
 *   [62..69] f64  deltaZ
 *   [70..77] f64  velX
 *   [78..85] f64  velY
 *   [86..93] f64  velZ
 *   [94]     u8   flags (bit0=onGround, bit1=inVehicle, bit2=inLiquid,
 *                        bit3=recentTeleport, bit4=recentKnockback, bit5=sprinting,
 *                        bit6=sneaking)
 *   [95..96] u16  attackCount
 *   [97..98] u16  blockUseCount
 *   [99..100]u16  itemUseCount
 *   [101..102]u16 digActionCount
 *   [103..104]u16 pingBucket
 *   [105]    u8   skippedTicks
 *   [106..109]f32 yaw
 *   [110..113]f32 pitch
 *   [114..127]    reserved (zero-padded)
 */
public final class PlayerTickSnapshot {

    public static final int SCHEMA_VERSION = 1;
    public static final int BYTE_SIZE = 128;

    public int tick;
    public long playerIdMsb;
    public long playerIdLsb;

    public double posX, posY, posZ;
    public double deltaX, deltaY, deltaZ;
    public double velX, velY, velZ;

    public boolean onGround;
    public boolean inVehicle;
    public boolean inLiquid;
    public boolean recentTeleport;
    public boolean recentKnockback;
    public boolean sprinting;
    public boolean sneaking;

    public int attackCount;
    public int blockUseCount;
    public int itemUseCount;
    public int digActionCount;

    public int pingBucket;
    public int skippedTicks;

    public float yaw;
    public float pitch;

    public void setPlayerId(UUID id) {
        this.playerIdMsb = id.getMostSignificantBits();
        this.playerIdLsb = id.getLeastSignificantBits();
    }

    public byte encodeFlags() {
        int f = 0;
        if (onGround)        f |= 1;
        if (inVehicle)       f |= 1 << 1;
        if (inLiquid)        f |= 1 << 2;
        if (recentTeleport)  f |= 1 << 3;
        if (recentKnockback) f |= 1 << 4;
        if (sprinting)       f |= 1 << 5;
        if (sneaking)        f |= 1 << 6;
        return (byte) f;
    }

    /**
     * Serialize into the provided buffer starting at the current position.
     * Caller must ensure at least BYTE_SIZE bytes remaining.
     */
    public void writeTo(ByteBuffer buf) {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int start = buf.position();

        buf.putShort((short) SCHEMA_VERSION);
        buf.putInt(tick);
        buf.putLong(playerIdMsb);
        buf.putLong(playerIdLsb);

        buf.putDouble(posX);
        buf.putDouble(posY);
        buf.putDouble(posZ);
        buf.putDouble(deltaX);
        buf.putDouble(deltaY);
        buf.putDouble(deltaZ);
        buf.putDouble(velX);
        buf.putDouble(velY);
        buf.putDouble(velZ);

        buf.put(encodeFlags());

        buf.putShort((short) attackCount);
        buf.putShort((short) blockUseCount);
        buf.putShort((short) itemUseCount);
        buf.putShort((short) digActionCount);

        buf.putShort((short) pingBucket);
        buf.put((byte) skippedTicks);

        buf.putFloat(yaw);
        buf.putFloat(pitch);

        int written = buf.position() - start;
        for (int i = written; i < BYTE_SIZE; i++) {
            buf.put((byte) 0);
        }
    }

    /**
     * Resets all mutable counters for reuse next tick without reallocating.
     */
    public void resetCounters() {
        attackCount = 0;
        blockUseCount = 0;
        itemUseCount = 0;
        digActionCount = 0;
        skippedTicks = 0;
        recentTeleport = false;
        recentKnockback = false;
    }
}

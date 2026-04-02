package ac.reaper.schema;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Fixed-layout binary frame returned from Rust scoring engine to Java enforcement.
 *
 * Layout (little-endian, 32 bytes fixed):
 *   [0..7]   u64  playerIdMsb
 *   [8..15]  u64  playerIdLsb
 *   [16..19] f32  riskScore     (0.0 .. 1.0)
 *   [20..23] f32  confidence    (0.0 .. 1.0)
 *   [24]     u8   action        (0=NONE, 1=FLAG, 2=SETBACK, 3=KICK)
 *   [25..26] u16  reasonCode
 *   [27..31]      reserved
 */
public final class ActionResponse {

    public static final int BYTE_SIZE = 32;

    public long playerIdMsb;
    public long playerIdLsb;
    public float riskScore;
    public float confidence;
    public Action action;
    public int reasonCode;

    public enum Action {
        NONE(0),
        FLAG(1),
        SETBACK(2),
        KICK(3);

        public final int code;

        Action(int code) { this.code = code; }

        public static Action fromCode(int code) {
            return switch (code) {
                case 1 -> FLAG;
                case 2 -> SETBACK;
                case 3 -> KICK;
                default -> NONE;
            };
        }
    }

    public static ActionResponse readFrom(ByteBuffer buf) {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        var resp = new ActionResponse();
        resp.playerIdMsb = buf.getLong();
        resp.playerIdLsb = buf.getLong();
        resp.riskScore = buf.getFloat();
        resp.confidence = buf.getFloat();
        resp.action = Action.fromCode(Byte.toUnsignedInt(buf.get()));
        resp.reasonCode = Short.toUnsignedInt(buf.getShort());
        buf.position(buf.position() + 5); // skip reserved
        return resp;
    }
}

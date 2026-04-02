package ac.reaper.bridge;

import ac.reaper.schema.ActionResponse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Encodes batch headers and decodes response frames for the Unix domain socket protocol.
 *
 * Wire protocol (both directions):
 *   [4 bytes LE] count
 *   [count * frame_size bytes] frames
 */
public final class BridgeCodec {

    private BridgeCodec() {}

    public static byte[] encodeBatchHeader(int count) {
        return ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(count)
                .array();
    }

    public static int decodeBatchHeader(byte[] header) {
        return ByteBuffer.wrap(header)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
    }

    public static ActionResponse decodeResponse(byte[] frame) {
        return ActionResponse.readFrom(
                ByteBuffer.wrap(frame).order(ByteOrder.LITTLE_ENDIAN)
        );
    }
}

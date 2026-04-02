package ac.reaper.capture;

import ac.reaper.config.ReaperConfig;
import ac.reaper.schema.PlayerTickSnapshot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Bounded ring-buffer of serialized {@link PlayerTickSnapshot} frames.
 *
 * Written to on the server thread during end-of-tick flush; drained by the
 * bridge thread. The single-producer / single-consumer design lets us use a
 * simple volatile head/tail without locks.
 */
public final class TickSnapshotBuffer {

    private final byte[][] slots;
    private volatile int head; // next write index (producer)
    private volatile int tail; // next read index (consumer)
    private final int capacity;

    public TickSnapshotBuffer() {
        this(ReaperConfig.BUFFER_CAPACITY);
    }

    public TickSnapshotBuffer(int capacity) {
        this.capacity = capacity;
        this.slots = new byte[capacity][];
    }

    /**
     * Enqueue a snapshot (server thread). If full, the oldest entry is silently
     * dropped (head advances) to maintain bounded memory.
     */
    public void offer(PlayerTickSnapshot snap) {
        var buf = ByteBuffer.allocate(PlayerTickSnapshot.BYTE_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        snap.writeTo(buf);

        int h = head;
        int nextH = (h + 1) % capacity;

        if (nextH == tail) {
            // Buffer full: drop oldest
            tail = (tail + 1) % capacity;
        }

        slots[h] = buf.array();
        head = nextH;
    }

    /**
     * Drain all available frames into the provided output buffer.
     * Returns the number of frames written. Caller provides a buffer
     * large enough for {@code capacity * BYTE_SIZE}.
     */
    public int drainTo(ByteBuffer out) {
        int count = 0;
        while (tail != head) {
            byte[] frame = slots[tail];
            if (frame != null && out.remaining() >= PlayerTickSnapshot.BYTE_SIZE) {
                out.put(frame);
                slots[tail] = null;
                count++;
            }
            tail = (tail + 1) % capacity;
        }
        return count;
    }

    public int size() {
        int h = head;
        int t = tail;
        return (h >= t) ? (h - t) : (capacity - t + h);
    }

    public boolean isEmpty() {
        return head == tail;
    }
}

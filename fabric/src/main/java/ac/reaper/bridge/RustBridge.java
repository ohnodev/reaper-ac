package ac.reaper.bridge;

import ac.reaper.capture.TickSnapshotBuffer;
import ac.reaper.config.ReaperConfig;
import ac.reaper.schema.ActionResponse;
import ac.reaper.schema.PlayerTickSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dedicated daemon thread that drains the {@link TickSnapshotBuffer}, sends batches
 * to the Rust scoring engine over a Unix domain socket, and collects responses
 * for the {@link ac.reaper.enforce.EnforcementPipeline}.
 *
 * If the engine is unreachable or slow, the bridge degrades to no-op mode
 * (snapshots are still captured for observability but no enforcement actions fire).
 */
public final class RustBridge implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger("ReaperAC-Bridge");

    private final TickSnapshotBuffer buffer;
    private final List<ActionResponse> pendingResponses =
            Collections.synchronizedList(new ArrayList<>());
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile boolean connected;

    public RustBridge(TickSnapshotBuffer buffer) {
        this.buffer = buffer;
    }

    public boolean isConnected() {
        return connected;
    }

    /** Drain collected responses; called from server thread. */
    public List<ActionResponse> drainResponses() {
        if (pendingResponses.isEmpty()) return List.of();
        var snapshot = new ArrayList<>(pendingResponses);
        pendingResponses.clear();
        return snapshot;
    }

    public void shutdown() {
        running.set(false);
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                connectAndProcess();
            } catch (Exception e) {
                connected = false;
                if (running.get()) {
                    LOG.warn("Bridge disconnected, retrying in 2s: {}", e.getMessage());
                    sleep(2000);
                }
            }
        }
        LOG.info("Bridge thread stopped");
    }

    private void connectAndProcess() throws IOException {
        var addr = UnixDomainSocketAddress.of(ReaperConfig.bridgeSocketPath);
        try (var channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(addr);
            connected = true;
            LOG.info("Connected to Rust engine at {}", ReaperConfig.bridgeSocketPath);

            OutputStream out = Channels.newOutputStream(channel);
            InputStream in = Channels.newInputStream(channel);

            var drainBuf = ByteBuffer.allocate(
                    ReaperConfig.BUFFER_CAPACITY * PlayerTickSnapshot.BYTE_SIZE
            ).order(ByteOrder.LITTLE_ENDIAN);

            while (running.get() && channel.isConnected()) {
                drainBuf.clear();
                int count = buffer.drainTo(drainBuf);

                if (count == 0) {
                    // Heartbeat: send zero-count header
                    out.write(BridgeCodec.encodeBatchHeader(0));
                    out.flush();
                    // Read heartbeat response
                    byte[] respHeader = in.readNBytes(4);
                    if (respHeader.length < 4) break;
                    sleep(50); // idle poll interval
                    continue;
                }

                long startNs = System.nanoTime();

                out.write(BridgeCodec.encodeBatchHeader(count));
                drainBuf.flip();
                byte[] payload = new byte[drainBuf.remaining()];
                drainBuf.get(payload);
                out.write(payload);
                out.flush();

                byte[] respHeader = in.readNBytes(4);
                if (respHeader.length < 4) break;
                int respCount = BridgeCodec.decodeBatchHeader(respHeader);

                for (int i = 0; i < respCount; i++) {
                    byte[] frame = in.readNBytes(ActionResponse.BYTE_SIZE);
                    if (frame.length < ActionResponse.BYTE_SIZE) break;
                    pendingResponses.add(BridgeCodec.decodeResponse(frame));
                }

                long elapsed = System.nanoTime() - startNs;
                if (elapsed > ReaperConfig.BRIDGE_TIMEOUT_NS) {
                    LOG.warn("Bridge round-trip {}ms exceeds budget, degrading",
                            elapsed / 1_000_000);
                }
            }
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

package ac.reaper;

import ac.reaper.bridge.RustBridge;
import ac.reaper.capture.FabricHooks;
import ac.reaper.capture.TickSnapshotBuffer;
import ac.reaper.enforce.EnforcementPipeline;
import ac.reaper.perf.TickProfiler;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric server-side entrypoint for ReaperAC.
 *
 * Lifecycle:
 *   1. Register capture hooks (Fabric events).
 *   2. Start bridge daemon thread to Rust scoring engine.
 *   3. Register enforcement tick handler with profiling.
 *   4. On server stop, shut down bridge cleanly.
 */
public final class ReaperMod implements DedicatedServerModInitializer {

    private static final Logger LOG = LoggerFactory.getLogger("ReaperAC");
    private RustBridge bridge;
    private Thread bridgeThread;

    @Override
    public void onInitializeServer() {
        LOG.info("ReaperAC v3.0.0 initializing");

        var buffer = new TickSnapshotBuffer();
        var hooks = new FabricHooks(buffer);
        hooks.register();

        bridge = new RustBridge(buffer);
        var enforcement = new EnforcementPipeline(bridge, hooks.states());
        var profiler = new TickProfiler();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            bridgeThread = new Thread(bridge, "ReaperAC-Bridge");
            bridgeThread.setDaemon(true);
            bridgeThread.start();
            LOG.info("Bridge thread started");
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            profiler.begin();
            enforcement.processTick(server);
            profiler.end();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOG.info("Shutting down ReaperAC");
            bridge.shutdown();
            if (bridgeThread != null) {
                bridgeThread.interrupt();
            }
        });

        LOG.info("ReaperAC initialized");
    }
}

package ac.reaper.perf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight server-thread profiler that tracks per-tick overhead
 * introduced by ReaperAC capture + enforcement.
 *
 * Accumulates timing samples and logs a summary every N ticks.
 * No allocations in the hot path beyond the initial array.
 */
public final class TickProfiler {

    private static final Logger LOG = LoggerFactory.getLogger("ReaperAC-Perf");
    private static final int WINDOW = 600; // ~30 seconds at 20 tps

    private final long[] samples = new long[WINDOW];
    private int index;
    private int count;
    private long tickStartNs;

    /** Call at the very start of ReaperAC's end-of-tick work. */
    public void begin() {
        tickStartNs = System.nanoTime();
    }

    /** Call at the very end of ReaperAC's end-of-tick work. */
    public void end() {
        long elapsed = System.nanoTime() - tickStartNs;
        samples[index] = elapsed;
        index = (index + 1) % WINDOW;
        if (count < WINDOW) count++;

        if (index == 0 && count == WINDOW) {
            report();
        }
    }

    private void report() {
        long sum = 0;
        long max = 0;
        long p95val = 0;

        long[] sorted = new long[count];
        System.arraycopy(samples, 0, sorted, 0, count);
        java.util.Arrays.sort(sorted);

        for (long s : sorted) {
            sum += s;
            if (s > max) max = s;
        }

        int p95idx = (int) (count * 0.95);
        p95val = sorted[Math.min(p95idx, count - 1)];

        double avgUs = (sum / (double) count) / 1_000.0;
        double maxUs = max / 1_000.0;
        double p95Us = p95val / 1_000.0;

        LOG.info("Tick overhead [{}]: avg={:.1}µs  p95={:.1}µs  max={:.1}µs",
                count, avgUs, p95Us, maxUs);

        if (p95Us > 1000.0) {
            LOG.warn("p95 tick overhead exceeds 1ms budget!");
        }
    }
}

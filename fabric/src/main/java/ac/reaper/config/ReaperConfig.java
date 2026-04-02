package ac.reaper.config;

/**
 * Central feature flags and tuning knobs.
 * Loaded once at startup; hot-reloadable in a future iteration.
 */
public final class ReaperConfig {

    private ReaperConfig() {}

    /** When false, the bridge will not send snapshots; the mod becomes observe-only telemetry. */
    public static volatile boolean rustEngineRequired = false;

    /** Maximum number of per-player snapshots buffered before the oldest is dropped. */
    public static final int BUFFER_CAPACITY = 512;

    /** Path for the Unix domain socket the Rust engine listens on. */
    public static volatile String bridgeSocketPath = "/tmp/reaper-anticheat.sock";

    /**
     * If bridge round-trip exceeds this (ns), degrade to observe-only for the rest
     * of the tick batch to protect server mspt.
     */
    public static final long BRIDGE_TIMEOUT_NS = 5_000_000L; // 5 ms

    /** Risk score threshold at which a FLAG action becomes a SETBACK. */
    public static final float SETBACK_THRESHOLD = 0.70f;

    /** Risk score threshold at which a SETBACK escalates to a KICK. */
    public static final float KICK_THRESHOLD = 0.95f;

    /** Ticks a player is immune after a teleport or respawn (avoids false positives). */
    public static final int TELEPORT_GRACE_TICKS = 5;

    /** Ticks a player is immune after receiving knockback. */
    public static final int KNOCKBACK_GRACE_TICKS = 3;
}

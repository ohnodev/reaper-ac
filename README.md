# ReaperAC

Low-overhead Minecraft anticheat for Fabric, powered by a Rust scoring engine.

## Architecture

```
FabricHooks (Java) → TickSnapshotBuffer → RustEngine (anticheat-core) → EnforcementPipeline (Java)
```

- **Fabric capture layer** collects per-player state from server tick events with constant-time field copies.
- **Bounded ring buffer** batches snapshots for the Rust engine with backpressure and drop policy.
- **Rust scoring engine** (`anticheat-core/`) evaluates movement, combat, and interaction heuristics.
- **Enforcement pipeline** applies flag/setback/kick actions on the server thread.
- **Unix domain socket** connects Java and Rust with graceful degradation if the engine is unavailable.

## Building

### Java (Fabric mod)

```bash
./gradlew :fabric:build
```

Output: `fabric/build/libs/reaperac-fabric-*.jar`

### Rust (scoring engine)

```bash
cd anticheat-core
cargo build --release
```

Output: `anticheat-core/target/release/anticheat-core`

## Running

1. Start the Rust engine: `REAPER_SOCKET=/tmp/reaper-anticheat.sock ./anticheat-core`
2. Place the Fabric jar in your server's `mods/` directory.
3. Start the Minecraft server.

The mod connects to the engine automatically. If the engine is unavailable, the mod degrades to observe-only mode.

## Configuration

Feature flags and thresholds are in `ac.reaper.config.ReaperConfig`. Key settings:

| Setting | Default | Description |
|---|---|---|
| `rustEngineRequired` | `false` | If true, server enforces even without engine |
| `bridgeSocketPath` | `/tmp/reaper-anticheat.sock` | Unix domain socket path |
| `BRIDGE_TIMEOUT_NS` | `5ms` | Max bridge round-trip before degradation |
| `SETBACK_THRESHOLD` | `0.70` | Risk score for setback action |
| `KICK_THRESHOLD` | `0.95` | Risk score for kick action |

## License

GPL-3.0

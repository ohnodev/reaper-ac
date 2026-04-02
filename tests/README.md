# Reaper + PacketEvents Harness

This harness validates Reaper + PacketEvents startup across:

- `reaper-pe-official-261` -> MC `26.1` (official PacketEvents path, Java 25)
- `reaper-pe-paper-1206` -> Paper `1.20.6` (older-version Bukkit path)
- `reaper-pe-spigot-1211` -> Spigot `1.21.1` (Bukkit path)

Note: this PR branch targets MC 26.1 only (`minecraft >= 26.1` in mod metadata), so 1.21.x startup is intentionally out of scope here.

## Prerequisites

- `bash`, `curl`, `python3`
- Java 21+ (and Java 25 for 26.1 official case)
- PacketEvents repo available locally at `/root/packetevents-reaper-restructure` or pass `--packetevents-repo`
- cloud-fabric published locally once:

```bash
git clone https://github.com/Incendo/cloud-minecraft-modded.git
cd cloud-minecraft-modded
./gradlew :cloud-fabric:publishToMavenLocal
```

## Run All Cases

From this repository root:

```bash
./tests/scripts/run-all-reaper-pe-harnesses.sh
```

Run Bukkit-only cases:

```bash
./tests/scripts/run-all-reaper-pe-bukkit-harnesses.sh
```

Run full stack (Fabric + Bukkit):

```bash
./tests/scripts/run-all-reaper-pe-stack-harnesses.sh
```

## Individual Cases

Intermediary 1.21.6:

```bash
./tests/scripts/bootstrap-reaper-pe-intermediary-1216.sh
./tests/scripts/run-server.sh --server-dir tests/runs/reaper-pe-1.21.6
```

Official 26.1:

```bash
./tests/scripts/bootstrap-reaper-pe-official-261.sh
./tests/scripts/run-server.sh --server-dir tests/runs/reaper-pe-26.1 --java-bin /path/to/java25
```

Bukkit Paper older-version (1.20.6):

```bash
./tests/scripts/bootstrap-reaper-pe-bukkit-paper-1206.sh
./tests/scripts/run-bukkit-server.sh --server-dir tests/runs/reaper-pe-paper-1.20.6
```

Bukkit Spigot (1.21.1):

```bash
./tests/scripts/bootstrap-reaper-pe-bukkit-spigot-1211.sh
./tests/scripts/run-bukkit-server.sh --server-dir tests/runs/reaper-pe-spigot-1.21.1
```

## Notes

- Defaults remain pinned for reproducibility (exact PacketEvents snapshot coordinate in `libs.versions.toml`).
- Local dependency behavior is opt-in via `-PmavenLocalOverride=true`/`MAVEN_LOCAL_OVERRIDE=true`.

<div align="center">
 <h1>ReaperAC</h1>

 <div>
  <a href="https://github.com/ohnodev/reaper-ac/actions/workflows/build.yml">
   <img alt="Build" src="https://img.shields.io/github/actions/workflow/status/ohnodev/reaper-ac/build.yml?style=flat&logo=github&label=build"/>
  </a>&nbsp;&nbsp;
  <a href="https://smp.thecabal.app">
   <img alt="Website" src="https://img.shields.io/badge/website-smp.thecabal.app-4caf50?style=flat">
  </a>&nbsp;&nbsp;
  <a href="https://discord.gg/2NR3W7j4vP">
   <img alt="Discord" src="https://img.shields.io/badge/discord-Cabal%20SMP-5865F2?style=flat&logo=discord&logoColor=white">
  </a>
 </div>
 <br>
</div>

ReaperAC is an open-source Minecraft anticheat designed to support the latest versions of Minecraft.
This fork targets **Minecraft 26.2 Snapshot 2 (Fabric-only)** with a vendored PacketEvents monorepo, aligned with the live minecraft-cabal server runtime. For current snapshot/release status, see [Minecraft Java release notes](https://www.minecraft.net/en-us/article/minecraft-26-2-snapshot-2) and [this repository's releases](https://github.com/ohnodev/reaper-ac/releases).
Geyser players are fully exempt from the anticheat to prevent false positives.

## Downloads

- Latest updates:
  - **Modrinth:** coming soon (release pipeline pending)
  - **GitHub Releases:** coming soon
  - **Download stats:** will be shown via live badges once public release listings are active

## Requirements & Installation

- **Java 25** or higher (required by MC 26.2 / Fabric Loader).
- A Fabric server environment running Minecraft 26.2 Snapshot 2.
- **Fabric Loader:** 0.19.1+ for MC 26.2 Snapshot 2.
- **Fabric API:** use a build that matches the MC 26.2 snapshot line (minecraft-cabal currently runs `fabric-api-0.145.5+26.2-snapshot-1.jar`).
- References:
  - Fabric Loader releases: https://github.com/FabricMC/fabric-loader/releases/tag/0.19.1
  - Fabric API releases: https://github.com/FabricMC/fabric-api/releases
  - Older API line (26.1.1, not the 26.2 snapshot target): https://github.com/FabricMC/fabric-api/releases/tag/0.145.4%2B26.1.1

## Version Policy

- ReaperAC in this repo is **latest-only**.
- Public release artifacts use the stable `26.2.x` version line (no branch/hash suffixes).
- We only support the current upstream Minecraft line (currently the 26.2 snapshot line). See [release notes](https://www.minecraft.net/en-us/article/minecraft-26-2-snapshot-2) for the latest snapshot status.
- Backports are intentionally out of scope; historical support will be handled via tagged releases later.

## Resources

- Server website: [smp.thecabal.app](https://smp.thecabal.app)
- For community support and project discussion join our [Discord](https://discord.gg/2NR3W7j4vP).

## Pull Requests

See [Contributing](CONTRIBUTING.md) for more information about contributing and what our guidelines
are.

## Build From Source

This fork targets **Minecraft 26.2 Snapshot 2 (Fabric-only)**, with PacketEvents vendored as a
Gradle composite build under `vendor/packetevents/`.

### Prerequisites

- **Java 25** or newer (`java -version` — required by MC 26.2 / Fabric Loader)
- Git
- Internet access for Gradle dependencies

### Build

The vendored PacketEvents is declared as a Gradle composite build via
`includeBuild("vendor/packetevents")` in `settings.gradle.kts`, so Gradle
automatically substitutes PE dependencies with the local source during
compilation.

For Fabric packaging, this repo now hooks `:fabric:processIncludeJars` to run
`vendor/packetevents:publishToMavenLocal` automatically, so PacketEvents source
edits are always included in the final Reaper jar with a normal build command.

```bash
git clone https://github.com/ohnodev/reaper-ac.git
cd reaper-ac

# Build the Reaper-AC Fabric jar (also publishes vendored PE automatically)
./gradlew :fabric:build -x test
```

If you only change code under `common/` or `fabric/` (not PE), you can skip
any extra PE-specific steps and just run `./gradlew :fabric:build -x test`.

CI workflows (`build.yml`, `build-and-publish.yml`, `codeql-analysis.yml`) can
still run the explicit PE publish step, but local developer builds no longer
require it.

### Windows (PowerShell)

```powershell
.\gradlew.bat :fabric:build -x test
```

### Build artifacts

- Fabric runtime jar: `fabric/build/libs/reaperac-fabric-<version>.jar`
- Extra artifacts (sources/javadocs): `fabric/build/libs/`

## Prebuilt Artifact In This Repo

This fork includes prebuilt Fabric runtime jars in `prebuilt/` for direct deployment.

Current prebuilt file (latest):

- `prebuilt/reaperac-fabric-26.2.0.jar`
- SHA-256: `06b56fbc0856f15704b785196b71fd17c48828cd6f05c5728bd92a3032a372b6`

Verify integrity:

```bash
sha256sum prebuilt/reaperac-fabric-26.2.0.jar
```

Update this checksum in the README whenever the prebuilt jar is rebuilt.

To inspect locally produced files after a build:

```bash
ls -lah fabric/build/libs/
```

## Snapshot Mapping Remap Runbook (Critical)

Use this runbook on every Minecraft snapshot bump where blockstate IDs may drift.
The goal is to regenerate PacketEvents blockstate mapping from native server IDs so
Reaper predictions/collisions do not decode to wrong blocks.

### What must be remapped

- **Primary file:** `vendor/packetevents/mappings/data/block_state/V_26_2.json`
- This file controls PacketEvents global blockstate ID -> state decoding order.
- If this order drifts from native runtime IDs, simulation will decode wrong blocks
  (example failure: `leaf_litter` decoding as `big_dripleaf`).

### Source of truth

- Native runtime mapping from the server:
  - `Block.getId(state)` -> `state.toString()`
- We dump this from a live Fabric server with the debug command:
  - `/dumpblockstates`

### Step-by-step procedure

1. **Dump native blockstates from server runtime**

   - Ensure `cabal-mobs` has the debug command `dumpblockstates` registered.
   - Run via console/RCON:

   ```bash
   dumpblockstates
   ```

   - Output is written under server `debug/`, for example:
   - `minecraft-cabal/server/debug/blockstate-id-map-YYYYMMDD-HHMMSS.txt`

2. **Generate helper artifacts (optional but recommended)**

   - Keep copies under:
   - `mapping-artifacts/26.2-snapshot-3/`
   - Recommended files:
     - `id-to-state-26.2-snapshot-3.json`
     - `state-to-id-26.2-snapshot-3.json`
     - `blockstate-id-map-26.2-snapshot-3.csv`

3. **Rebuild `V_26_2.json` from native dump**

   - Convert the native dump into PacketEvents `block_state` format:
     - ordered by increasing native state ID
     - grouped by `type`
     - preserve existing `def` index per type when still in range
   - Write output to:
   - `vendor/packetevents/mappings/data/block_state/V_26_2.json`

4. **Sanity-check key IDs**

   - Verify known problematic IDs decode correctly after remap.
   - Example checks used for 26.2-snapshot-3:
     - `30350 -> Block{minecraft:leaf_litter}[facing=east,segment_amount=4]`
     - `30352 -> Block{minecraft:big_dripleaf}[facing=north,tilt=none,waterlogged=true]`

5. **Validate build + mappings**

   ```bash
   ./gradlew compileJava
   ./gradlew :packetevents:api:test --tests com.github.retrooper.packetevents.test.MappingIntegrityTest
   ./gradlew :fabric:build
   ```

6. **Deploy**

   - Copy built jar:
   - `fabric/build/libs/reaperac-fabric-26.2.0.jar`
   - Into server mods:
   - `minecraft-cabal/server/mods/reaperac-fabric-26.2.0.jar`
   - Restart server and verify with trace logs on previously failing blocks.

### Upstream format reference

- Grim PacketEvents repository (mapping structure reference):
  - [GrimAnticheat/packetevents](https://github.com/GrimAnticheat/packetevents)

<div align="center">
 <h1>ReaperAC</h1>

 <div>
  <a href="https://github.com/GrimAnticheat/Grim/actions/workflows/gradle-publish.yml">
   <img alt="Workflow" src="https://img.shields.io/github/actions/workflow/status/GrimAnticheat/Grim/gradle-publish.yml?style=flat&logo=github"/>
  </a>&nbsp;&nbsp;
  <a href="https://modrinth.com/plugin/grimac">
   <img alt="Modrinth" src="https://img.shields.io/modrinth/v/LJNGWSvH?style=flat&label=version&logo=modrinth">
  </a>&nbsp;&nbsp;
  <a href="https://modrinth.com/plugin/grimac#download">
   <img alt="Downloads" src="https://img.shields.io/modrinth/dt/LJNGWSvH?style=flat&logo=modrinth&label=downloads&link=https%3A%2F%2Fmodrinth.com%2Fplugin%2Fgrimac%23download">
  </a>&nbsp;&nbsp;
  <a href="https://discord.grim.ac">
   <img alt="Discord" src="https://img.shields.io/discord/811396969670901800?style=flat&label=discord&logo=discord">
  </a>
 </div>
 <br>
</div>

ReaperAC is an open-source Minecraft anticheat designed to support the latest versions of Minecraft.
This fork targets **Minecraft 26.2 (Fabric-only)** with a vendored PacketEvents monorepo.
Geyser players are fully exempt from the anticheat to prevent false positives.

## Downloads

- Latest updates:
  - **[Modrinth](https://modrinth.com/plugin/grimac)** *(recommended)*
  - GitHub artifacts: [Fabric](https://nightly.link/GrimAnticheat/Grim/workflows/gradle-publish/2.0/grimac-fabric.zip) *(bleeding edge)*

## Requirements & Installation

- **Java 25** or higher (required by MC 26.2 / Fabric Loader).
- A Fabric server environment running Minecraft 26.2.

## Resources

- For documentation and examples visit the [Wiki](https://github.com/GrimAnticheat/Grim/wiki).
- For answers to commonly asked questions visit the [FAQ](https://github.com/GrimAnticheat/Grim/wiki/FAQ).
- For community support and project discussion join our [Discord](https://discord.grim.ac).

## Pull Requests

See [Contributing](CONTRIBUTING.md) for more information about contributing and what our guidelines
are.

## Developer Plugin API

Grim's plugin API allows you to integrate Grim into your own plugins. Visit
the [plugin API repository](https://github.com/GrimAnticheat/GrimAPI) for the source code and more
information.

## Build From Source

This fork targets **Fabric-only, Minecraft 26.2**, with PacketEvents vendored as a
Gradle composite build under `vendor/packetevents/`.

### Prerequisites

- **Java 25** or newer (`java -version` — required by MC 26.2 / Fabric Loader)
- Git
- Internet access for Gradle dependencies

### Build

The vendored PacketEvents is declared as a Gradle composite build via
`includeBuild("vendor/packetevents")` in `settings.gradle.kts`, so Gradle
automatically substitutes PE dependencies with the local source during
compilation — no manual publish step is needed for most workflows.

However, Fabric Loom's jar-in-jar (JIJ) packaging resolves PE from Maven
Local (not the composite build), so the PE artifacts must be published there
before assembling the final Fabric jar.

```bash
git clone https://github.com/ohnodev/reaper-ac.git
cd reaper-ac

# Publish PE to Maven Local (required for Fabric Loom JIJ packaging)
./gradlew -p vendor/packetevents clean publishToMavenLocal

# Build the Reaper-AC Fabric jar
./gradlew :fabric:build -x test
```

If you only change code under `common/` or `fabric/` (not PE), you can skip
the first command and just run `./gradlew :fabric:build -x test`.

CI workflows (`build.yml`, `build-and-publish.yml`, `codeql-analysis.yml`)
automatically run the PE publish step before the main build.

### Windows (PowerShell)

```powershell
.\gradlew.bat -p vendor\packetevents clean publishToMavenLocal
.\gradlew.bat :fabric:build -x test
```

### Build artifacts

- Fabric runtime jar: `fabric/build/libs/reaperac-fabric-<version>.jar`
- Extra artifacts (sources/javadocs): `fabric/build/libs/`

## Prebuilt Artifact In This Repo

This fork includes prebuilt Fabric artifacts in `prebuilt/` for direct deployment.

Current prebuilt files:

- `prebuilt/reaperac-fabric-2.3.74-26.2-b567b7e.jar`
- `prebuilt/reaperac-fabric-2.3.74-26.2-b567b7e.zip`
- `prebuilt/reaperac-fabric-2.3.74-26.2-b567b7e.jar.sha256`
- `prebuilt/reaperac-fabric-2.3.74-26.2-b567b7e.zip.sha256`

Verify integrity:

```bash
sha256sum -c prebuilt/reaperac-fabric-2.3.74-26.2-b567b7e.jar.sha256
sha256sum -c prebuilt/reaperac-fabric-2.3.74-26.2-b567b7e.zip.sha256
```

To inspect locally produced files after a build:

```bash
ls -lah fabric/build/libs/
```

## Grim Supremacy

What makes Grim stand out against other anticheats?

### Movement Simulation Engine

* We have a 1:1 replication of the player's possible movements
    * This covers everything from basic walking, swimming, knockback, cobwebs, to bubble columns
    * It even covers riding entities from boats to pigs to striders
* Built upon covering edge cases to confirm accuracy
* 1.13+ clients on 1.13+ servers, 1.12- clients on 1.13+ servers, 1.13+ clients on 1.12- servers,
  and 1.12- clients on 1.12- servers are all supported regardless of the large technical changes
  between these versions.
* The order of collisions depends on the client version and is correct
* Accounts for minor bounding box differences between versions, for example:
    * Single glass panes will be a + shape for 1.7-1.8 players and * for 1.9+ players
    * 1.13+ clients on 1.8 servers see the + glass pane hitbox due to ViaVersion
    * Many other blocks have this extreme attention to detail.
    * Waterlogged blocks do not exist for 1.12 or below players
    * Blocks that do not exist in the client's version use ViaVersion's replacement block
    * Block data that cannot be translated to previous versions is replaced correctly
    * All vanilla collision boxes have been implemented

### Fully asynchronous and multithreaded design

* All movement checks and the overwhelming majority of listeners run on the netty thread
* The anticheat can scale to many hundreds of players, if not more
* Thread safety is carefully thought out
* The next core allows for this design

### Full world replication

* The anticheat keeps a replica of the world for each player
* The replica is created by listening to chunk data packets, block places, and block changes
* On all versions, chunks are compressed to 16-64 kb per chunk using palettes
* Using this cache, the anticheat can safely access the world state
* Per player, the cache allows for multithreaded design
* Sending players fake blocks with packets is safe and does not lead to falses
* The world is recreated for each player to allow lag compensation
* Client sided blocks cause no issues with packet based blocks. Block glitching does not false the
  anticheat.

### Latency compensation

* World changes are queued until they reach the player
* This means breaking blocks under a player does not false the anticheat
* Everything from flying status to movement speed will be latency compensated

### Inventory compensation

* The player's inventory is tracked to prevent ghost blocks at high latency, and other errors

### Secure by design, not obscurity

* All systems are designed to be highly secure and mathematically impossible to bypass
* For example, the prediction engine knows all possible movements and cannot be bypassed

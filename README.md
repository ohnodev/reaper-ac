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

## Version Policy

- ReaperAC in this repo is **latest-only**.
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

This fork includes one final combined Fabric runtime jar in `prebuilt/` for direct deployment.

Current prebuilt file:

- `prebuilt/reaperac-fabric-2.3.74-f6690b4.jar`

To inspect locally produced files after a build:

```bash
ls -lah fabric/build/libs/
```

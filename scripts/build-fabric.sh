#!/usr/bin/env bash
# Canonical Reaper Fabric build. Vendored PacketEvents is published to Maven Local
# automatically via :fabric:processIncludeJars -> vendoredPacketEventsPublishTasks
# (see fabric/build.gradle.kts). No separate PE publish step is required.
#
# Usage:
#   ./scripts/build-fabric.sh
#
# Optional: copy the runtime jar into your cabal server mods after build:
#   export MINECRAFT_CABAL_SERVER_MODS="/path/to/minecraft-cabal/server/mods"
#   ./scripts/build-fabric.sh
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ -z "${JAVA_HOME:-}" ]]; then
  for candidate in \
    /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
    /usr/lib/jvm/java-21-openjdk-amd64; do
    if [[ -x "${candidate}/bin/java" ]]; then
      export JAVA_HOME="${candidate}"
      break
    fi
  done
fi

./gradlew :fabric:build -x test

VERSION="$(./gradlew -q printVersion | sed -n 's/^VERSION=//p')"
JAR="${ROOT}/fabric/build/libs/reaperac-fabric-${VERSION}.jar"
if [[ ! -f "$JAR" ]]; then
  echo "[build-fabric] ERROR: expected jar missing: $JAR" >&2
  exit 1
fi
echo "[build-fabric] OK: $JAR"

if [[ -n "${MINECRAFT_CABAL_SERVER_MODS:-}" ]]; then
  mkdir -p "${MINECRAFT_CABAL_SERVER_MODS}"
  cp -f "$JAR" "${MINECRAFT_CABAL_SERVER_MODS}/"
  echo "[build-fabric] Copied to ${MINECRAFT_CABAL_SERVER_MODS}/"
fi

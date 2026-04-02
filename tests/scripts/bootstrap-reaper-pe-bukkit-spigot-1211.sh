#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
PACKETEVENTS_REPO="${PACKETEVENTS_REPO:-/root/packetevents-reaper-restructure}"

cd "${REPO_ROOT}"

echo "[bootstrap:bukkit-spigot-1211] Building Reaper Bukkit artifact..."
./gradlew -PmavenLocalOverride=true :bukkit:shadowJar

echo "[bootstrap:bukkit-spigot-1211] Building PacketEvents Spigot artifact..."
./gradlew -p "${PACKETEVENTS_REPO}" :spigot:build

echo "[bootstrap:bukkit-spigot-1211] Setting up Spigot server (1.21.1)..."
"${SCRIPT_DIR}/setup-spigot-server.sh" \
  --mc-version 1.21.1 \
  --server-dir tests/runs/reaper-pe-spigot-1.21.1

echo "[bootstrap:bukkit-spigot-1211] Installing Reaper + PacketEvents plugins..."
"${SCRIPT_DIR}/install-reaper-pe-bukkit-jars.sh" \
  --server-dir tests/runs/reaper-pe-spigot-1.21.1 \
  --packetevents-repo "${PACKETEVENTS_REPO}"

echo
echo "[bootstrap:bukkit-spigot-1211] Ready."

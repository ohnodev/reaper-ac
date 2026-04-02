#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "[all-stack] Running Fabric 26.1 harness..."
"${SCRIPT_DIR}/run-all-reaper-pe-harnesses.sh"

echo "[all-stack] Running Bukkit harnesses (Paper older-version + Spigot)..."
"${SCRIPT_DIR}/run-all-reaper-pe-bukkit-harnesses.sh"

echo "[all-stack] Full Reaper + PacketEvents stack harness complete."

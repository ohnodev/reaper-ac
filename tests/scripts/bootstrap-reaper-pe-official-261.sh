#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"${SCRIPT_DIR}/bootstrap-reaper-pe-case.sh" \
  --case reaper-pe-official-261 \
  --mc-version 26.1 \
  --server-dir tests/runs/reaper-pe-26.1 \
  --profile official-261 \
  "$@"

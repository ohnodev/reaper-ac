# Reaper AC

Reaper AC is our custom Grim-based anticheat repository for Fabric 26.1 only (single native protocol target, no Via translation support).

## Goals

- Keep a clean, controllable codebase for our own server requirements
- Prioritize stability and performance on a single native protocol path
- Iterate quickly on packet decode resiliency and production diagnostics

## Upstream Base

This repository is derived from Grim (2.0 branch) and remains GPL-3.0 licensed.

- Upstream Grim repository: https://github.com/GrimAnticheat/Grim
- Original project license: see `LICENSE`

## Build

```bash
./gradlew build
```

Artifacts are produced in each platform module's `build/libs` directory.

## Notes

- This repository starts with a fresh git history for our internal customization workflow.
- Core package names and module structure are initially preserved to minimize migration risk.

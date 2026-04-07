# CI Overview

## Active Workflows (`.github/workflows/`)

| Workflow | File | Trigger | Purpose |
|----------|------|---------|---------|
| **Build** | `build.yml` | Push to `main`, PRs to `main`, manual | Runs `./gradlew build` on ubuntu-latest with JDK 21. Uploads the Fabric jar as an artifact. |
| **CodeQL** | `codeql-analysis.yml` | Push to `main`, PRs to `main`, manual | Static security analysis for Java/Kotlin via GitHub CodeQL. |
| **Gradle Package (Dev)** | `gradle-publish.yml` | Push to `main`/`dev`/`release`, manual | Builds and publishes dev artifacts to Modrinth and the update API. Uses private runner + secrets. |
| **Publish Release** | `release.yml` | GitHub Release published | Builds and publishes a release artifact. Uses private runner + secrets. |
| **Build and Publish (reusable)** | `build-and-publish.yml` | Called by `gradle-publish.yml` and `release.yml` | Shared build/publish logic. Not triggered directly. |

## What "CI pass" Means

A green **Build** check confirms that `./gradlew build` succeeds on a clean ubuntu-latest runner with JDK 21. This catches compilation errors, resource processing failures, and Gradle configuration problems before they reach `main`.

## Removed Files

The following workflows were removed because they were historical artifacts imported via the `reaper-api` subtree squash. They lived under `reaper-api/.github/workflows/` which GitHub Actions does not execute (only root `.github/workflows/` is active). Their functionality is covered by the root workflows above.

- `reaper-api/.github/workflows/codeql.yml` -- duplicate CodeQL template with unused Swift/macOS runner logic.
- `reaper-api/.github/workflows/gradle-publish.yml` -- duplicate publish workflow targeting the old standalone GrimAPI repo.

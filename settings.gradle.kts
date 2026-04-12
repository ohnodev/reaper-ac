dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }

        create("testlibs") {
            from(files("testlibs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        // For the Fabric Loom plugin
        exclusiveContent {
            forRepository {
                maven {
                    name = "FabricMC"
                    url = uri("https://maven.fabricmc.net/")
                }
            }
            filter {
                includeModule("fabric-loom", "fabric-loom.gradle.plugin")
                includeModule("net.fabricmc.fabric-loom", "net.fabricmc.fabric-loom.gradle.plugin")
                includeGroupByRegex("net.fabricmc.*")
            }
        }

        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.gradle.develocity") version "4.2.1" apply false
}

if (gradle.startParameter.isBuildScan) {
    apply(plugin = "com.gradle.develocity")
    develocity {
        buildScan {
            // This is the magic part that bypasses the interactive "yes/no" prompt
            termsOfUseUrl = "https://gradle.com/terms-of-service"
            termsOfUseAgree = "yes"

            // Best practice for CI: ensure the scan finishes uploading before the step completes
            uploadInBackground = false

            // Automatically add useful tags and links to the scan
            if (System.getenv("CI") == "true") {
                tag("CI")
                link(
                    "GitHub Actions build",
                    System.getenv("GITHUB_SERVER_URL") + "/" + System.getenv("GITHUB_REPOSITORY") + "/actions/runs/" + System.getenv(
                        "GITHUB_RUN_ID"
                    )
                )
            }
        }
    }
}

rootProject.name = "grimac"
include("common")
include("bukkit")
include("grim-fabric-common")
// grim-fabric-intermediary/mc* sources stay in-repo for the PE-style layout. They are not Gradle-included yet:
// Loom 1.15 rejects official Mojang mappings on older MC lines, default mappings + fabric-api AWs conflict, and
// namedElements cross-slices changed. Re-enable include("grim-fabric-intermediary:mc1161") etc. when that graph is updated.
include("grim-fabric-official")
include("grim-fabric-official:mc261")
include("fabric")

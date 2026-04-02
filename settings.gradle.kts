dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
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

rootProject.name = "reaperac"
include("fabric")

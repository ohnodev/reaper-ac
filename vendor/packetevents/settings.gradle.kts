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
        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "packetevents"
include("api")
include("netty-common")
include("fabric")
include("fabric-common")
include("fabric-official")
// 26.1-only build: keep official Fabric path and required shared modules.
include(":patch:adventure-text-serializer-gson")

import versioning.BuildConfig

// Mapping- and Minecraft-version-agnostic Fabric helpers (PE-style fabric-common analogue).
plugins {
    `java-library`
    grim.`base-conventions`
}

repositories {
    exclusive("https://maven.fabricmc.net/") {
        includeGroup("net.fabricmc")
    }

    exclusive("https://repo.grim.ac/snapshots") {
        includeGroup("ac.grim.grimac")
    }
    maven("https://repo.grim.ac/snapshots") {
        mavenContent { snapshotsOnly() }
        content {
            includeGroup("com.github.retrooper")
        }
    }

    exclusive("https://repo.viaversion.com", { mavenContent { releasesOnly() } }) {
        includeGroup("com.viaversion")
    }

    exclusive("https://nexus.scarsz.me/content/repositories/releases", { mavenContent { releasesOnly() } }) {
        includeGroup("github.scarsz")
    }

    exclusive("https://repo.opencollab.dev/maven-releases/", { mavenContent { releasesOnly() } }) {
        includeGroup("org.geysermc.api")
    }

    exclusive("https://repo.opencollab.dev/maven-snapshots/", { mavenContent { snapshotsOnly() } }) {
        includeGroup("org.geysermc.floodgate")
        includeGroup("org.geysermc.cumulus")
        includeModule("org.geysermc", "common")
        includeModule("org.geysermc", "geyser-parent")
    }

    mavenCentral()

    exclusive("https://repo.codemc.io/repository/maven-releases/", { mavenContent { releasesOnly() } }) {
        includeGroup("com.github.retrooper")
    }

    if (BuildConfig.mavenLocalOverride) {
        mavenLocal()
    }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(libs.fabric.loader)
    compileOnly(libs.packetevents.api)
}

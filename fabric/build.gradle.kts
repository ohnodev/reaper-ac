import versioning.BuildConfig

val minecraft_version: String by project
val fabric_version: String by project

plugins {
    `maven-publish`
    alias(libs.plugins.fabric.loom)
    grim.`base-conventions`
    grim.`jij-conventions`
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    compileOnly("me.lucko:fabric-permissions-api:0.7.0")

    // cloud-fabric from local maven (26.1-compatible build)
    implementation("org.incendo:cloud-fabric:2.0.0-SNAPSHOT") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }

    implementation(libs.fabric.loader)

    // PacketEvents from local maven (26.1-compatible build)
    implementation("com.github.retrooper:packetevents-fabric:2.12.0-SNAPSHOT") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }
    compileOnly("com.github.retrooper:packetevents-fabric-common:2.12.0-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-fabric-official:2.12.0-SNAPSHOT") {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }

    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.apache.logging.log4j:log4j-api:2.24.3")

    implementation(project(":common"))
}

repositories {
    mavenLocal()

    exclusive("https://maven.fabricmc.net/") {
        includeGroup("net.fabricmc")
        includeGroup("net.fabricmc.fabric-api")
    }

    exclusive("https://repo.grim.ac/snapshots") {
        includeGroup("ac.grim.grimac")
    }

    exclusive("https://jitpack.io", { mavenContent { releasesOnly() } }) {
        includeGroup("com.github.Fallen-Breath.conditional-mixin")
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

    exclusive(mavenCentral()) { includeGroup("me.lucko") }

    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

loom {
    accessWidenerPath = file("src/main/resources/grimac.accesswidener")
}

publishing.publications.create<MavenPublication>("maven") {
    artifact(tasks["jar"])
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.name}-fabric")
        archiveVersion.set(rootProject.version as String)
    }
}

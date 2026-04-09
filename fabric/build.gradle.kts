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

    // Bundle permissions API so Fabric permission resolution is available at runtime.
    implementation("me.lucko:fabric-permissions-api:0.7.0")

    implementation(libs.fabric.loader)

    // Compile against PE API; the full PE fabric runtime is JIJ'd for single-jar deployment.
    compileOnly(libs.packetevents.api)
    include(libs.packetevents.fabric)

    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.apache.logging.log4j:log4j-api:2.24.3")

    implementation(project(":common"))
}

repositories {
    if (BuildConfig.mavenLocalOverride) {
        mavenLocal()
    }
    exclusive("https://maven.fabricmc.net/") {
        includeGroup("net.fabricmc")
        includeGroup("net.fabricmc.fabric-api")
    }

    exclusive("https://repo.grim.ac/snapshots") {
        includeGroup("ac.reaper.reaperac")
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

    exclusive(mavenCentral()) { includeGroup("me.lucko") }

    mavenCentral()
}

java {
    // Base conventions keep a lower default for cross-platform compatibility.
    // Fabric 26.1 runs on Java 25, so this module explicitly targets 25.
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

loom {
    accessWidenerPath = file("src/main/resources/grimac.accesswidener")
}

publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
}

tasks {
    jar {
        archiveBaseName.set("reaperac-fabric")
        archiveVersion.set(rootProject.version as String)
    }
}

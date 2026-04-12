import versioning.BuildConfig

val minecraft_version: String by project
val fabric_version: String by project

// PE-style aggregate: one published Fabric mod JAR; implementation lives in grim-fabric-* modules.
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

    implementation(libs.cloud.fabric) {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }

    implementation(libs.fabric.loader)

    compileOnly(libs.packetevents.api)

    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.apache.logging.log4j:log4j-api:2.24.3")

    implementation(project(":common"))
    implementation(project(":grim-fabric-common"))
    implementation(project(":grim-fabric-official:mc261"))
    include(project(":grim-fabric-common"))
    include(project(":grim-fabric-official:mc261"))
}

repositories {
    exclusive("https://maven.fabricmc.net/") {
        includeGroup("net.fabricmc")
        includeGroup("net.fabricmc.fabric-api")
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

    exclusive("https://repo.codemc.io/repository/maven-releases/", { mavenContent { releasesOnly() } }) {
        includeGroup("com.github.retrooper")
    }

    maven("https://central.sonatype.com/repository/maven-snapshots/") {
        content {
            includeGroup("org.incendo")
        }
    }

    if (BuildConfig.mavenLocalOverride) {
        mavenLocal()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

loom {
}

publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.name}-fabric")
        archiveVersion.set(rootProject.version as String)
    }
}

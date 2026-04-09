import versioning.BuildConfig

plugins {
    `maven-publish`
    grim.`base-conventions`
}

repositories {
    // Prefer locally published PacketEvents (e.g. cabal PE fork) over remote snapshots when enabled.
    if (BuildConfig.mavenLocalOverride) {
        mavenLocal()
    }
    // PacketEvents snapshots: same host but not exclusiveContent-locked so mavenLocal() can still resolve
    maven("https://repo.grim.ac/snapshots") {
        mavenContent { snapshotsOnly() }
        content {
            includeGroup("com.github.retrooper")
        }
    }

    // ViaVersion
    exclusive("https://repo.viaversion.com", { mavenContent { releasesOnly() } }) {
        includeGroup("com.viaversion")
    }

    // Configuralize
    exclusive("https://nexus.scarsz.me/content/repositories/releases", { mavenContent { releasesOnly() } }) {
        includeGroup("github.scarsz")
    }

    // Cumulus
    exclusive("https://repo.opencollab.dev/maven-releases/", { mavenContent { releasesOnly() } }) {
        includeGroup("org.geysermc.api")
    }

    mavenCentral()
}


dependencies {
    if (BuildConfig.shadePE) {
        api(libs.packetevents.api)
    } else {
        compileOnly(libs.packetevents.api)
    }
    api(libs.configuralize) {
        artifact {
            classifier = "slim"
        }
        exclude(group = "org.yaml", module = "snakeyaml")
    }
    api(libs.guava)
    api(libs.gson)
    // Bump snakeyaml (transitive dep of configuralize) 1.29 -> 2.2+ for geyser-fabric
    api(libs.snakeyaml)
    api(libs.fastutil)
    api(libs.adventure.text.minimessage)
    api(libs.jetbrains.annotations)
    api(libs.hikaricp)
    api(libs.grim.api)
    api(libs.grim.internal)
    compileOnly(libs.grim.internal.shims)

    compileOnly(libs.geyser.base.api) {
        isTransitive = false // messes with guava otherwise
    }

    compileOnly(libs.checker.qual)
    compileOnly(libs.jsr305)
    compileOnly(libs.viaversion)
    compileOnly(libs.netty)

    testImplementation(platform(testlibs.junit.bom))
    testImplementation(testlibs.junit.jupiter)
    testRuntimeOnly(testlibs.junit.platform.launcher)
    testImplementation(testlibs.mockito.core)
    testImplementation(testlibs.mockito.junit.jupiter)
    // Mockito's ByteBuddy needs ViaVersion on classpath for GrimPlayer instrumentation
    testImplementation(libs.viaversion)
    // PE static init chain needs real Netty buffers, adventure-nbt, and the netty bridge
    testImplementation(libs.netty)
    testImplementation(libs.packetevents.netty.common)
    testImplementation(libs.adventure.nbt)
    // PE 26.2 fork was built against adventure 4.25.0 — force that version at test runtime
    testImplementation("net.kyori:adventure-api:4.25.0")
    testImplementation("net.kyori:adventure-key:4.25.0")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.25.0")
    testImplementation("net.kyori:adventure-text-serializer-legacy:4.25.0")
    testImplementation("net.kyori:examination-api:1.3.0")
    testImplementation("net.kyori:examination-string:1.3.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
}

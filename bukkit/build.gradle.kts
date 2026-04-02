import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission
import versioning.BuildConfig

plugins {
    `maven-publish`
    reaper.`base-conventions`
    reaper.`shadow-conventions`
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1"
}

repositories {
    // 1. Fallback for non-exclusive deps (e.g. Maven Central deps)
    if (BuildConfig.mavenLocalOverride) mavenLocal()

    // 2. Exclusive Repositories (One HTTP request per dep)
    exclusive("https://repo.papermc.io/repository/maven-public/", { name = "papermc" }) {
        includeGroup("io.papermc.paper")
        includeGroup("net.md-5")
    }

    exclusive("https://libraries.minecraft.net", { mavenContent { releasesOnly() } }) {
        includeModule("com.mojang", "brigadier")
    }

    exclusive("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        includeGroup("me.clip")
    }

    exclusive("https://repo.grim.ac/snapshots") {
        includeGroup("ac.reaper")
        includeGroup("com.github.retrooper")
    }

    exclusive("https://nexus.scarsz.me/content/repositories/releases", { mavenContent { releasesOnly() } }) {
        includeGroup("github.scarsz")
    }

    mavenCentral()
}


dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)

    if (BuildConfig.shadePE) {
        implementation(libs.packetevents.spigot)
    } else {
        compileOnly(libs.packetevents.spigot)
    }
    implementation(libs.cloud.paper)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.reaper.bukkit.internal)

    implementation(project(":common"))
    shadow(project(":common"))
}

bukkit {
    name = "ReaperAC"
    author = "ReaperAC"
    main = "ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin"
    website = "https://reaper.ac/"
    apiVersion = "1.13"
    foliaSupported = true

    if (!BuildConfig.shadePE) {
        depend = listOf("packetevents")
    }

    softDepend = listOf(
        "ProtocolLib",
        "ProtocolSupport",
        "Essentials",
        "ViaVersion",
        "ViaBackwards",
        "ViaRewind",
        "Geyser-Spigot",
        "floodgate",
        "FastLogin",
        "PlaceholderAPI",
    )

    permissions {
        register("reaper.alerts") {
            description = "Receive alerts for violations"
            default = Permission.Default.OP
        }

        register("reaper.alerts.enable-on-join") {
            description = "Enable alerts on join"
            default = Permission.Default.OP
        }

        register("reaper.performance") {
            description = "Check performance metrics"
            default = Permission.Default.OP
        }

        register("reaper.profile") {
            description = "Check user profile"
            default = Permission.Default.OP
        }

        register("reaper.brand") {
            description = "Show client brands on join"
            default = Permission.Default.OP
        }

        register("reaper.brand.enable-on-join") {
            description = "Enable showing client brands on join"
            default = Permission.Default.OP
        }

        register("reaper.sendalert") {
            description = "Send cheater alert"
            default = Permission.Default.OP
        }

        register("reaper.nosetback") {
            description = "Disable setback"
            default = Permission.Default.FALSE
        }

        register("reaper.nomodifypacket") {
            description = "Disable modifying packets"
            default = Permission.Default.FALSE
        }

        register("reaper.exempt") {
            description = "Exempt from all checks"
            default = Permission.Default.FALSE
        }

        register("reaper.verbose") {
            description = "Receive verbose alerts for violations"
            default = Permission.Default.OP
        }

        register("reaper.verbose.enable-on-join") {
            description =
                "Enable verbose alerts on join"
            default = Permission.Default.FALSE
        }

        register("reaper.list") {
            description =
                "Shows lists of specific data"
            default = Permission.Default.FALSE
        }

    }
}

publishing.publications.create<MavenPublication>("maven") {
    artifact(tasks["shadowJar"])
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
    }

    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }
}

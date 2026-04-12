import versioning.BuildConfig

// Version-sliced compile-only stubs (namedElements) for cross-version Fabric references.
subprojects {
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

        maven {
            name = "lucko"
            url = uri("https://repo.lucko.me/")
            content {
                includeGroup("me.lucko")
            }
        }

        mavenCentral()

        exclusive("https://repo.codemc.io/repository/maven-releases/", { mavenContent { releasesOnly() } }) {
            includeGroup("com.github.retrooper")
        }

        if (BuildConfig.mavenLocalOverride) {
            mavenLocal()
        }
    }
}

rootProject.name = "ReaperAPI"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include(":reaper-internal")
include(":reaper-internal-shims")
include(":reaper-bukkit-internal")
include(":reaper-fabric-internal")
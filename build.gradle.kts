group = "ac.reaper"
version = "3.0.0-SNAPSHOT"
description = "Low-overhead Fabric anticheat with Rust scoring engine."

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
    }
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import versioning.BuildConfig

plugins {
    id("com.gradleup.shadow")
}

tasks.named<ShadowJar>("shadowJar") {
    minimize()
    archiveFileName = "${rootProject.name}-${project.name}-${rootProject.version}.jar"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    if (BuildConfig.relocate) {
        val shadedPrefix = "ac.reaper.reaperac.shaded."
        if (BuildConfig.shadePE) {
            relocate("io.github.retrooper.packetevents", shadedPrefix + "io.github.retrooper.packetevents")
            relocate("com.github.retrooper.packetevents", shadedPrefix + "com.github.retrooper.packetevents")
            relocate("net.kyori", shadedPrefix + "kyori") // use PE's built-in adventure instead when not shading PE
        }
        relocate("club.minnced", shadedPrefix + "discord-webhooks")
        relocate("org.slf4j", shadedPrefix + "slf4j") // Required by discord-webhooks
        relocate("github.scarsz.configuralize", shadedPrefix + "configuralize")
        relocate("com.github.puregero", shadedPrefix + "com.github.puregero")
        relocate("com.google.code.gson", shadedPrefix + "gson")
        relocate("alexh", shadedPrefix + "maps")
        relocate("it.unimi.dsi.fastutil", shadedPrefix + "fastutil")
        relocate("okhttp3", shadedPrefix + "okhttp3")
        relocate("okio", shadedPrefix + "okio")
        relocate("org.yaml.snakeyaml", shadedPrefix + "snakeyaml")
        relocate("org.json", shadedPrefix + "json")
        relocate("org.intellij", shadedPrefix + "intellij")
        relocate("org.jetbrains", shadedPrefix + "jetbrains")
        relocate("org.incendo", shadedPrefix + "incendo")
        relocate("io.leangen.geantyref", shadedPrefix + "geantyref") // Required by cloud
        relocate("com.zaxxer", shadedPrefix + "zaxxer") // Database history
    }
    mergeServiceFiles()
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}

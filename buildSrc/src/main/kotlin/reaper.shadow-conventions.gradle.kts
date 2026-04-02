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
        if (BuildConfig.shadePE) {
            relocate("io.github.retrooper.packetevents", "ac.reaper.shaded.io.github.retrooper.packetevents")
            relocate("com.github.retrooper.packetevents", "ac.reaper.shaded.com.github.retrooper.packetevents")
            relocate("net.kyori", "ac.reaper.shaded.kyori") // use PE's built-in adventure instead when not shading PE
        }
        relocate("club.minnced", "ac.reaper.shaded.discord-webhooks")
        relocate("org.slf4j", "ac.reaper.shaded.slf4j") // Required by discord-webhooks
        relocate("github.scarsz.configuralize", "ac.reaper.shaded.configuralize")
        relocate("com.github.puregero", "ac.reaper.shaded.com.github.puregero")
        relocate("com.google.code.gson", "ac.reaper.shaded.gson")
        relocate("alexh", "ac.reaper.shaded.maps")
        relocate("it.unimi.dsi.fastutil", "ac.reaper.shaded.fastutil")
        relocate("okhttp3", "ac.reaper.shaded.okhttp3")
        relocate("okio", "ac.reaper.shaded.okio")
        relocate("org.yaml.snakeyaml", "ac.reaper.shaded.snakeyaml")
        relocate("org.json", "ac.reaper.shaded.json")
        relocate("org.intellij", "ac.reaper.shaded.intellij")
        relocate("org.jetbrains", "ac.reaper.shaded.jetbrains")
        relocate("org.incendo", "ac.reaper.shaded.incendo")
        relocate("io.leangen.geantyref", "ac.reaper.shaded.geantyref") // Required by cloud
        relocate("com.zaxxer", "ac.reaper.shaded.zaxxer") // Database history
    }
    mergeServiceFiles()
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}

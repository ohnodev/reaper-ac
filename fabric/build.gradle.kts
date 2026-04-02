val minecraft_version: String by project
val fabric_version: String by project

plugins {
    alias(libs.plugins.fabric.loom)
    `java-library`
}

repositories {
    maven("https://maven.fabricmc.net/") {
        content {
            includeGroup("net.fabricmc")
            includeGroup("net.fabricmc.fabric-api")
        }
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
    implementation(libs.fabric.loader)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

loom {
    accessWidenerPath = file("src/main/resources/reaperac.accesswidener")
}

tasks.jar {
    archiveBaseName.set("${rootProject.name}-fabric")
    archiveVersion.set(rootProject.version as String)
}

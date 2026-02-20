pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Este plugin permite ao Gradle descarregar automaticamente o JDK 21 (Foojay Resolver)
    // É a forma moderna de resolver o erro "Undefined Toolchain Download Repositories"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "template-engine"


import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.0.0"
    id("pl.allegro.tech.build.axion-release") version "1.13.14"
    id("io.freefair.lombok") version "6.5.0.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

group = "tech.kronicle.dependencies-file"
description = "Gradle plugin for generating a gradle-dependencies.yaml file"
version = if (System.getenv("CI") != null) scmVersion.version else "0.0.1"

gradlePlugin {
    plugins {
        create("dependenciesFilePlugin") {
            id = "tech.kronicle.dependencies-file"
            implementationClass = "tech.kronicle.dependenciesfile.gradle.DependenciesFilePlugin"
            displayName = "Dependencies File"
            description = """
                This plugin outputs a `gradle-dependencies.yaml` file that can be committed to your Git repo and read by tools like Kronicle.  
            """.trimIndent()
        }
    }
}

pluginBundle {
    website = "https://github.com/kronicle-tech/dependencies-file-gradle-plugin"
    vcsUrl = "https://github.com/kronicle-tech/dependencies-file-gradle-plugin.git"
    tags = listOf("dependencies", "file", "lock")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

tasks.compileJava {
    options.release.set(8)
}

apply(from = "gradle/kronicle-metadata.gradle.kts")

dependencies {
    shadow(localGroovy())
    shadow(gradleApi())

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")
}

tasks {
    named<ShadowJar>("shadowJar") {
        classifier = null
        dependsOn(":relocateShadowJar")
    }

    register<ConfigureShadowRelocation>("relocateShadowJar") {
        target = named<ShadowJar>("shadowJar").get()
    }
}

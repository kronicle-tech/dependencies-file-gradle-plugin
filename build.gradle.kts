plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.0.0"
    id("pl.allegro.tech.build.axion-release") version "1.13.14"
}

repositories {
    mavenCentral()
}

group = "tech.kronicle.dependencies-file"
description "Gradle plugin for generating a gradle-dependencies.yaml file"

gradlePlugin {
    plugins {
        create("simplePlugin") {
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
    tags = listOf("gradle", "dependencies")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}
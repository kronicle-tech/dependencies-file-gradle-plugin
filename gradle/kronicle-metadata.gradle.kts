import tech.kronicle.KronicleMetadataValidator

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("tech.kronicle:kronicle-metadata:0.1.310")
    }
}

tasks.register("validateKronicleMetadata") {
    doLast {
        KronicleMetadataValidator.validate(file("kronicle.yaml"))
    }
}

tasks.named("build") { dependsOn("validateKronicleMetadata") }

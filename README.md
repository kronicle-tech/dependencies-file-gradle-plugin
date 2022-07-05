# Dependencies File Gradle Plugin

Gradle plugin that generates `gradle-dependencies.yaml` files.  

A `gradle-dependencies.yaml` file is typically committed to a codebase's Git repo and provides a record of the 
dependencies used by a Gradle codebase.  See 
https://github.com/kronicle-tech/dependencies-file-gradle-plugin/blob/main/gradle-dependencies.yaml for an example.  

The `gradle-dependencies.yaml` file is similar to npm's `package-lock.json` file, except it is only used to record the 
dependencies used by a codebase and unlike `package-lock.json` is not used to "lock" those dependencies.  

The `gradle-dependencies.yaml` file can be useful: 

1. When trying to resolve a dependency issue that is causing Java `ClassNotFoundException` errors
2. When reviewing a pull request that includes a dependency upgrade (e.g. a version upgrade for a framework like Spring Boot), as way to see what other dependencies have changed version as a result
3. With tooling like Kronicle that visualises the important dependencies of various codebases


## Kronicle

Kronicle is an example of a tool that can use the `gradle-dependencies.yaml` files committed to a Git repo to find all 
the dependencies used by a codebase.  

See the following links for examples of Kronicle using `gradle-dependencies.yaml` files to visualise dependency versions:

1. Showing `Key Software` badges for the major software used by a codebase/component: https://demo.kronicle.tech/components/kronicle-service
2. Listing the "Key Software" used by a number of codebases/components: https://demo.kronicle.tech/all-components/key-software
3. Listing all the dependencies used by a codebase/component, including Gradle and npm dependencies: https://demo.kronicle.tech/components/kronicle-service/software
4. An API endpoint that lists the same dependencies used by a codebase/component: https://demo.kronicle.tech/api/v1/components/kronicle-service?stateType=softwares&fields=component(id,name,type,description,states)


## Usage

The plugin can be added to each project in a Gradle codebase:

**build.gradle (Groovy)**
```groovy
plugins {
    id "tech.kronicle.dependencies-file" version "v0.1.10"
}

subprojects {
    apply plugin: "tech.kronicle.dependencies-file"
}
```

**build.gradle.kts (Kotlin)**
```kotlin
plugins {
    id("tech.kronicle.dependencies-file") version "v0.1.10"
}

subprojects {
    apply(plugin = "tech.kronicle.dependencies-file")
}
```

The plugin provides a task that will generate a `gradle-dependencies.yaml` for each project and subproject that
the plugin is applied to: 

```shell
./gradlew generateDependenciesFile
```

If a project has a `build` task, the plugin will automatically add its `generateDependenciesFile` task as a dependency 
for the `build` task.  So everytime `./gradlew build` is run, the `gradle-dependencies.yaml` files will be regenerated.  

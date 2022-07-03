package tech.kronicle.dependenciesfile.gradle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import tech.kronicle.dependenciesfile.gradle.models.OutputConfiguration;
import tech.kronicle.dependenciesfile.gradle.models.OutputDependency;
import tech.kronicle.dependenciesfile.gradle.models.OutputRoot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class GenerateDependenciesFileTask extends DefaultTask {

    public static final String TASK_NAME = "generateDependenciesFile";
    public static final String GRADLE_DEPENDENCIES_FILE_NAME = "gradle-dependencies.yaml";

    @OutputFile
    public abstract Property<File> getGradleDependenciesFile();

    public GenerateDependenciesFileTask() {
        getGradleDependenciesFile().convention(getProject().file(GRADLE_DEPENDENCIES_FILE_NAME));
    }

    @SneakyThrows
    @TaskAction
    public void generateDependenciesFile() {
        writeGradleDependenciesFile(writeYaml(mapRoot(getProject().getConfigurations())));
    }

    private OutputRoot mapRoot(ConfigurationContainer configurations) {
        return OutputRoot.builder()
                .configurations(mapConfigurations(configurations))
                .build();
    }

    private List<OutputConfiguration> mapConfigurations(ConfigurationContainer configurations) {
        return configurations.stream()
                .sorted(Comparator.comparing(Configuration::getName))
                .map(this::mapConfiguration)
                .collect(toList());
    }

    private OutputConfiguration mapConfiguration(Configuration configuration) {
        return OutputConfiguration.builder()
                .name(configuration.getName())
                .dependencies(mapDependencies(configuration.getDependencies()))
                .build();
    }
    
    private List<OutputDependency> mapDependencies(DependencySet dependencies) {
        return dependencies.stream()
                .sorted(Comparator.comparing(Dependency::getName))
                .map(this::mapDependency)
                .collect(toList());
    }

    private OutputDependency mapDependency(Dependency dependency) {
        return OutputDependency.builder()
                .name(dependency.getName())
                .group(dependency.getGroup())
                .reason(dependency.getReason())
                .version(dependency.getVersion())
                .build();
    }

    private String writeYaml(OutputRoot root) throws JsonProcessingException {
        return new YAMLMapper().writeValueAsString(root);
    }

    private void writeGradleDependenciesFile(String yaml) throws IOException {
        File gradleDependenciesFile = getGradleDependenciesFile().get();
        Files.write(gradleDependenciesFile.toPath(), yaml.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
    }
}

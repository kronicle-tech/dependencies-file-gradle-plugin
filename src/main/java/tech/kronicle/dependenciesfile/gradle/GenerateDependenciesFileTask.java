package tech.kronicle.dependenciesfile.gradle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.*;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import tech.kronicle.dependenciesfile.gradle.models.OutputConfiguration;
import tech.kronicle.dependenciesfile.gradle.models.OutputDependency;
import tech.kronicle.dependenciesfile.gradle.models.OutputResolvedDependency;
import tech.kronicle.dependenciesfile.gradle.models.OutputRoot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
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
        List<OutputResolvedDependency> resolvedDependencies = mapResolvedDependencies(configuration);
        return OutputConfiguration.builder()
                .name(configuration.getName())
                .description(configuration.getDescription())
                .visible(configuration.isVisible())
                .canBeResolved(configuration.isCanBeResolved())
                .resolved(Objects.nonNull(resolvedDependencies))
                .dependencies(mapDependencies(configuration.getDependencies()))
                .resolvedDependencies(resolvedDependencies)
                .build();
    }

    private List<OutputDependency> mapDependencies(DependencySet dependencies) {
        if (dependencies.isEmpty()) {
            return null;
        }
        return dependencies.stream()
                .sorted(Comparator.comparing(Dependency::getName))
                .map(this::mapDependency)
                .collect(toList());
    }

    private OutputDependency mapDependency(Dependency dependency) {
        return OutputDependency.builder()
                .name(getDependencyName(dependency))
                .reason(dependency.getReason())
                .build();
    }

    private String getDependencyName(Dependency dependency) {
        return Stream.of(dependency.getGroup(), dependency.getName(), dependency.getVersion())
                .filter(Objects::nonNull)
                .collect(joining(":"));
    }

    private List<OutputResolvedDependency> mapResolvedDependencies(Configuration configuration) {
        if (!configuration.isCanBeResolved()) {
            return null;
        }
        ResolvedConfiguration resolvedConfiguration = configuration.getResolvedConfiguration();
        if (resolvedConfiguration.hasError()) {
            return null;
        }
        Set<ResolvedDependency> directDependencies = resolvedConfiguration.getFirstLevelModuleDependencies();
        List<ResolvedDependency> nonLeafTransitiveDependencies = getNonLeafTransitiveDependencies(
                directDependencies
        );
        return mapResolvedDependencies(directDependencies, nonLeafTransitiveDependencies);
    }

    private List<ResolvedDependency> getNonLeafTransitiveDependencies(Set<ResolvedDependency> directDependencies) {
        Queue<ResolvedDependency> queue = new LinkedList<>(directDependencies);
        List<ResolvedDependency> nonLeafTransitiveDependencies = new ArrayList<>();

        while (!queue.isEmpty()) {
            ResolvedDependency dependency = queue.remove();

            for (ResolvedDependency child : dependency.getChildren()) {
                if (!child.getChildren().isEmpty()) {
                    nonLeafTransitiveDependencies.add(child);
                    queue.add(child);
                }
            }
        }

        return nonLeafTransitiveDependencies;
    }

    private List<OutputResolvedDependency> mapResolvedDependencies(
            Set<ResolvedDependency> directDependencies,
            List<ResolvedDependency> nonLeafTransitiveDependencies
    ) {
        if (directDependencies.isEmpty()) {
            return null;
        }
        return Stream.of(
                        mapResolvedDependencies(directDependencies, true, true),
                        mapResolvedDependencies(nonLeafTransitiveDependencies, false, true)
                )
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private List<OutputResolvedDependency> mapResolvedDependencies(
            Collection<ResolvedDependency> dependencies,
            boolean direct,
            boolean mapChildren
    ) {
        return dependencies.stream()
                .sorted(Comparator.comparing(ResolvedDependency::getName))
                .map(dependency -> mapResolvedDependency(dependency, direct, mapChildren))
                .collect(toList());
    }

    private OutputResolvedDependency mapResolvedDependency(
            ResolvedDependency dependency,
            boolean direct,
            boolean mapChildren
    ) {
        return OutputResolvedDependency.builder()
                .name(dependency.getName())
                .direct(mapDirect(direct))
                .dependencies(mapChildren
                        ? mapResolvedDependencies(dependency.getChildren(), false, false)
                        : null)
                .build();
    }

    private Boolean mapDirect(boolean direct) {
        return direct ? true : null;
    }

    private String writeYaml(OutputRoot root) throws JsonProcessingException {
        return new YAMLMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(root);
    }

    private void writeGradleDependenciesFile(String yaml) throws IOException {
        File gradleDependenciesFile = getGradleDependenciesFile().get();
        Files.write(gradleDependenciesFile.toPath(), yaml.getBytes(StandardCharsets.UTF_8));
    }
}

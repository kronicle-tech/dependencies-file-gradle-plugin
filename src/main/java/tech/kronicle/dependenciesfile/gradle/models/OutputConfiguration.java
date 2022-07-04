package tech.kronicle.dependenciesfile.gradle.models;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OutputConfiguration {

    String name;
    String description;
    Boolean visible;
    Boolean canBeResolved;
    Boolean resolved;
    List<OutputDependency> dependencies;
    List<OutputResolvedDependency> resolvedDependencies;
}

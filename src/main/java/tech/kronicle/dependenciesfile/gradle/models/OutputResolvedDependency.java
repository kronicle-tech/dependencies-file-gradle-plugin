package tech.kronicle.dependenciesfile.gradle.models;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OutputResolvedDependency {

    String name;
    String moduleName;
    String moduleGroup;
    String moduleVersion;
    List<OutputResolvedDependency> resolvedDependencies;
}

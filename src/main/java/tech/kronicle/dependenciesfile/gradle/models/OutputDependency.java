package tech.kronicle.dependenciesfile.gradle.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OutputDependency {

    String name;
    String reason;
}

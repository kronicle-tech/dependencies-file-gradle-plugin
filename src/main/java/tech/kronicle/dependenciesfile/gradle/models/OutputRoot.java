package tech.kronicle.dependenciesfile.gradle.models;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OutputRoot {

    List<OutputConfiguration> configurations;
}

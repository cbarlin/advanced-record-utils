package io.github.cbarlin.aru.core.impl.types;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.micronaut.sourcegen.javapoet.TypeName;

public interface OptionalComponent<T extends AnalysedComponent> {

    T component();

    default boolean isIntendedConstructorParam() {
        return component().isIntendedConstructorParam();
    }

    default String name() {
        return component().name();
    }

    default ToBeBuilt builderArtifact() {
        return component().builderArtifact();
    }

    default TypeName unNestedPrimaryTypeName() {
        return component().unNestedPrimaryTypeName();
    }
}

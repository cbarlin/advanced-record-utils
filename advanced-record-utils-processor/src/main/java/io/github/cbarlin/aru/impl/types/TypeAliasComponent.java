package io.github.cbarlin.aru.impl.types;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

import java.util.Optional;

public record TypeAliasComponent(
    AnalysedComponent delegate,
    TypeName aliasFor,
    ClassName aliasClassName,
    String valueMethodName
) implements DelegatingComponent {

    @Override
    public TypeName serialisedTypeName() {
        return aliasFor;
    }

    @Override
    public Optional<ClassName> className() {
        return Optional.of(aliasClassName);
    }
}

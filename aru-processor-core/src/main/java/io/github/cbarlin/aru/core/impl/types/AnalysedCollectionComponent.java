package io.github.cbarlin.aru.core.impl.types;

import java.util.Optional;
import java.util.function.Consumer;

import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

public record AnalysedCollectionComponent (
    AnalysedComponent delegate,
    TypeMirror innerType,
    TypeName innerTypeName,
    ClassName erasedWrapperClassName
) implements DelegatingComponent {

    private static final String UNWRAPPING_VARIABLE_NAME = "__innerValue";

    public AdvRecUtilsSettings settings() {
        return delegate.parentRecord().settings();
    }

    @Override
    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.of(erasedWrapperClassName);
    }

    @Override
    public TypeMirror unNestedPrimaryComponentType() {
        return innerType;
    }

    @Override
    public TypeName unNestedPrimaryTypeName() {
        return innerTypeName;
    }

    @Override
    public boolean requiresUnwrapping() {
        return true;
    }

    @Override
    public boolean isLoopable() {
        return true;
    }

    @Override
    public TypeName serialisedTypeName() {
        // Always consider a collection of any type a simple list
        return ParameterizedTypeName.get(Names.LIST, innerTypeName);
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedType) {
        methodBuilder.beginControlFlow("for (final $T $L : $L)", unwrappedType, UNWRAPPING_VARIABLE_NAME, incomingName);
        withUnwrappedName.accept(UNWRAPPING_VARIABLE_NAME);
        methodBuilder.endControlFlow();
    }

    @Override
    public final String toString() {
        return delegate().toString();
    }
}

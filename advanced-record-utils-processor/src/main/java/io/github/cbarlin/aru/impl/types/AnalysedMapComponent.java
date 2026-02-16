package io.github.cbarlin.aru.impl.types;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;
import io.github.cbarlin.aru.impl.Constants;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Handles e.g. {@code Map<A, B>}
 */
public record AnalysedMapComponent(
    AnalysedComponent delegate,
    DeclaredType mapTypeMirror,
    TypeName mapTypeName,
    ClassName erasedMapTypeName,
    TypeMirror unNestedPrimaryComponentType,
    TypeName unNestedPrimaryTypeName,
    TypeMirror unNestedSecondaryComponentType,
    TypeName unNestedSecondaryTypeName,
    ClassName mutableTypeName
) implements DelegatingComponent {
    private static final String MAP_ELEMENT = "__mapElement";

    @Override
    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.of(erasedMapTypeName);
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
        return ParameterizedTypeName.get(erasedMapTypeName, unNestedPrimaryTypeName, unNestedSecondaryTypeName);
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedTypeName) {
        final ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(Constants.Names.MAP_ENTRY, unNestedPrimaryTypeName, unNestedSecondaryTypeName);
        methodBuilder.beginControlFlow("for (final $T $L : $L.entrySet())", parameterizedTypeName, MAP_ELEMENT, incomingName);
        withUnwrappedName.accept(MAP_ELEMENT);
        methodBuilder.endControlFlow();
    }
}

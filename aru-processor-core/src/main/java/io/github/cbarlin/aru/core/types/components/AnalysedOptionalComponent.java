package io.github.cbarlin.aru.core.types.components;

import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Consumer;

public record AnalysedOptionalComponent(
    AnalysedComponent delegate,
    TypeMirror innerType,
    TypeName innerTypeName
) implements OptionalComponent {

    private static final String UNWRAPPING_VARIABLE_NAME = "__innerValue";

    @Override
    public TypeMirror unNestedPrimaryComponentType() {
        return innerType;
    }

    @Override
    public TypeName unNestedPrimaryTypeName() {
        return innerTypeName;
    }

    @Override
    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.of(CommonsConstants.Names.OPTIONAL);
    }

    @Override
    public boolean requiresUnwrapping() {
        return true;
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedTn) {
        methodBuilder.beginControlFlow("if ($L.isPresent())", incomingName)
            .addStatement("final $T $L = $L.get()", unwrappedTn, UNWRAPPING_VARIABLE_NAME, incomingName);
        withUnwrappedName.accept(UNWRAPPING_VARIABLE_NAME);
        methodBuilder.endControlFlow();
    }

    @Override
    public final String toString() {
        return delegate().toString();
    }

    @Override
    public TypeName serialisedTypeName() {
        return innerTypeName;
    }
}

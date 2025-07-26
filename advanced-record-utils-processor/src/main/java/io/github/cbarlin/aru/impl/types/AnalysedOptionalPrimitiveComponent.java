package io.github.cbarlin.aru.impl.types;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_DOUBLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_INT;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_LONG;

/**
 * An analysed component for optional primitives like {@link java.util.OptionalInt}
 */

public record AnalysedOptionalPrimitiveComponent(
    AnalysedComponent delegate,
    TypeName innerTypeName,
    TypeMirror innerTypeMirror
) implements DelegatingComponent {
    public static final Map<TypeName, String> TYPE_TO_GETTER = Map.of(
        OPTIONAL_LONG, "getAsLong",
        OPTIONAL_INT, "getAsInt",
        OPTIONAL_DOUBLE, "getAsDouble"
    );

    public static String getterMethod(final TypeName typeName) {
        return Objects.requireNonNull(TYPE_TO_GETTER.get(typeName), "AnalysedOptionalPrimitiveComponent applied incorrectly and no getter method can be found");
    }

    public String getterMethod() {
        return getterMethod(delegate.typeName());
    }

    @Override
    public TypeMirror unNestedPrimaryComponentType() {
        return innerTypeMirror;
    }

    @Override
    public TypeName unNestedPrimaryTypeName() {
        return innerTypeName;
    }

    @Override
    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.empty();
    }

    @Override
    public boolean requiresUnwrapping() {
        return true;
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedTn) {
        final String methodName = getterMethod();
        methodBuilder.beginControlFlow("if ($T.nonNull($L) && $L.isPresent())", OBJECTS, incomingName, incomingName)
            .addStatement("final $T $L = $L.$L()", unwrappedTn, "__innerPrimitive", incomingName, methodName);
        withUnwrappedName.accept("__innerPrimitive");
        methodBuilder.endControlFlow();
    }
}

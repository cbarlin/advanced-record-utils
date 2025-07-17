package io.github.cbarlin.aru.impl.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_DOUBLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_INT;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_LONG;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

/**
 * An analysed component for optional primitives like {@link java.util.OptionalInt}
 */
public final class AnalysedOptionalPrimitiveComponent extends AnalysedComponent {

    public static final Map<TypeName, String> TYPE_TO_GETTER = Map.of(
        OPTIONAL_LONG, "getAsLong",
        OPTIONAL_INT, "getAsInt",
        OPTIONAL_DOUBLE, "getAsDouble"
    );

    private final TypeName innerTypeName;
    private final TypeMirror innerType;

    public AnalysedOptionalPrimitiveComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord,
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        super(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        
        if (OPTIONAL_LONG.equals(typeName)) {
            innerTypeName = TypeName.LONG;
            innerType = APContext.types().getPrimitiveType(TypeKind.LONG);
        } else if (OPTIONAL_DOUBLE.equals(typeName)) {
            innerTypeName = TypeName.DOUBLE;
            innerType = APContext.types().getPrimitiveType(TypeKind.DOUBLE);
        } else if (OPTIONAL_INT.equals(typeName)) {
            innerTypeName = TypeName.INT;
            innerType = APContext.types().getPrimitiveType(TypeKind.INT);
        } else {
            throw new IllegalArgumentException("Attempt to construct an optional primitive analysis on a non-optional primitive");
        }
    }

    public static String getterMethod(TypeName typeName) {
        return Objects.requireNonNull(TYPE_TO_GETTER.get(typeName), "AnalysedOptionalPrimitiveComponent applied incorrectly and no getter method can be found");
    }

    public String getterMethod() {
        return getterMethod(typeName);
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

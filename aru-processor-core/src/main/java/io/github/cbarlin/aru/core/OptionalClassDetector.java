package io.github.cbarlin.aru.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.micronaut.sourcegen.javapoet.ArrayTypeName;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

public class OptionalClassDetector {

    private static final Map<TypeName, Optional<TypeElement>> DETECTED_MAP = new HashMap<>();

    private OptionalClassDetector() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Optional<TypeElement> detectClassName(final ClassName toDetect) {
        try {
            return Optional.ofNullable(APContext.elements().getTypeElement(toDetect.canonicalName()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<TypeElement> detectOptionalDependency(final TypeName typeName) {
        return switch (typeName) {
            case ClassName cn -> detectClassName(cn);
            case ParameterizedTypeName ptn -> detectClassName(ptn.rawType);
            case ArrayTypeName atn when (!atn.isPrimitive()) && atn.componentType instanceof ClassName cn -> detectClassName(cn);
            case null, default -> Optional.empty();
        };
    }

    public static Optional<TypeElement> optionalDependencyTypeElement(final TypeName typeName) {
        return DETECTED_MAP.computeIfAbsent(typeName, OptionalClassDetector::detectOptionalDependency);
    }

    public static boolean doesDependencyExist(final TypeName typeName) {
        return optionalDependencyTypeElement(typeName).isPresent();
    }

    public static Optional<TypeMirror> optionalDependencyTypeMirror(final TypeName typeName) {
        return optionalDependencyTypeElement(typeName).map(TypeElement::asType);
    }

    public static Predicate<TypeMirror> checkSameOrSubType(final TypeName optionalDependencyTypeName) {
        final var optionalTypeMirror = optionalDependencyTypeMirror(optionalDependencyTypeName);
        if (optionalTypeMirror.isEmpty()) {
            return in -> false;
        } else {
            final var dependencyTypeMirror = optionalTypeMirror.get();
            final var types = APContext.types();
            return (TypeMirror returnType) -> types.isSubtype(types.erasure(returnType), types.erasure(dependencyTypeMirror));
        }
    }

    public static boolean checkSameOrSubType(final RecordComponentElement recordComponentElement, final TypeName optionalDepTypeName ) {
        return checkSameOrSubType(optionalDepTypeName).test(recordComponentElement.getAccessor().getReturnType());
    } 
}

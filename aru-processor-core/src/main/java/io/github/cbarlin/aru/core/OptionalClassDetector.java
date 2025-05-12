package io.github.cbarlin.aru.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import io.micronaut.sourcegen.javapoet.ArrayTypeName;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

/**
 * Utility for detecting dependencies and comparing them via TypeNames.
 * <p>
 * Doesn't neecessarily need to be optional dependencies either, I suppose.
 */
public class OptionalClassDetector {

    private static final Map<TypeName, Optional<TypeElement>> DETECTED_MAP = new ConcurrentHashMap<>();

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

    /**
     * Obtain a type element based on the provided type name
     * @param typeName The type name to attempt to convert into a TypeElement
     * @return The TypeElement, if found
     */
    public static Optional<TypeElement> optionalDependencyTypeElement(final TypeName typeName) {
        return DETECTED_MAP.computeIfAbsent(typeName, OptionalClassDetector::detectOptionalDependency);
    }

    /**
     * Detect if the given type name exists
     * @param typeName The type name to check
     * @return If it is known
     */
    public static boolean doesDependencyExist(final TypeName typeName) {
        return optionalDependencyTypeElement(typeName).isPresent();
    }

    /**
     * Obtain a type mirror based on the provided type name
     * @param typeName The type name to attempt to convert into a TypeElement
     * @return The TypeElement, if found
     */
    public static Optional<TypeMirror> optionalDependencyTypeMirror(final TypeName typeName) {
        return optionalDependencyTypeElement(typeName).map(TypeElement::asType);
    }

    /**
     * Create a predicate to determine if a given type mirror is the same or subtype of the provided typename
     * @param typeName The type name to create a predicate for
     * @return A testing predicate
     */
    public static Predicate<TypeMirror> checkSameOrSubType(final TypeName typeName) {
        final var optionalTypeMirror = optionalDependencyTypeMirror(typeName);
        if (optionalTypeMirror.isEmpty()) {
            return in -> false;
        } else {
            final TypeMirror dependencyTypeMirror = optionalTypeMirror.get();
            final Types types = APContext.types();
            final TypeMirror erasedDep = types.erasure(dependencyTypeMirror);
            return (TypeMirror returnType) -> {
                final TypeMirror erasedReturn = types.erasure(returnType);
                return types.isAssignable(erasedReturn, erasedDep) || types.isSubtype(erasedReturn, erasedDep) ;
            };
        }
    }

    /**
     * Check if the record component has the same type as the type name
     * @param recordComponentElement The component element to check
     * @param typeName The type name to compare against
     * @return True if the types are the same, or if the component is a subtype/assignable to.
     */
    public static boolean checkSameOrSubType(final RecordComponentElement recordComponentElement, final TypeName typeName ) {
        return checkSameOrSubType(typeName).test(recordComponentElement.asType());
    }

    /**
     * Check if the first type is the same as, a subtype of, or is otherwise assignable to the second
     * @param compareFrom The first type
     * @param compareTo The second type
     * @return Assignability or subtype check
     */
    public static boolean checkSameOrSubType(final TypeName compareFrom, final TypeName compareTo) {
        return Boolean.TRUE == optionalDependencyTypeMirror(compareTo)
            .flatMap(
                compTo -> optionalDependencyTypeMirror(compareFrom)
                    .map(compFrom -> {
                        final Types types = APContext.types();
                        final TypeMirror erasedFrom = types.erasure(compFrom);
                        final TypeMirror erasedTo = types.erasure(compTo);
                        return types.isAssignable(erasedFrom, erasedTo) || types.isSubtype(erasedFrom, erasedTo);
                    })
            )
            .orElse(Boolean.FALSE);
    }
}

package io.github.cbarlin.aru.core;

import io.github.cbarlin.aru.annotations.Generated;
import io.micronaut.sourcegen.javapoet.ArrayTypeName;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility for detecting if classes (or interfaces etc) exist and comparing them via TypeNames.
 */
public final class OptionalClassDetector {

    private static final Map<TypeName, Optional<TypeElement>> DETECTED_MAP = new HashMap<>();
    private static final Map<TypeName, Optional<TypeElement>> LOADED_ANNOTATIONS = new HashMap<>();

    @Generated("CONSTRUCTOR_STATIC_CLASS")
    private OptionalClassDetector() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static synchronized Optional<TypeElement> detectClassName(final ClassName toDetect) {
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
            default -> Optional.empty();
        };
    }

    private static Optional<TypeElement> loadAnnotation(final TypeName typeName) {
        final Optional<TypeElement> ret = optionalDependencyTypeElement(typeName);
        // No-op, but the side effect is that the item is loaded
        ret.ifPresent(typeElement -> ElementFilter.methodsIn(typeElement.getEnclosedElements()));
        return ret.filter(te -> ElementKind.ANNOTATION_TYPE.equals(te.getKind()));
    }

    /**
     * Obtain a type element based on the provided mirror
     * @param typeMirror The mirror to attempt to convert into a TypeElement
     * @return The TypeElement, if found
     */
    public static Optional<TypeElement> optionalDependencyTypeElement(final @Nullable TypeMirror typeMirror) {
        return Optional.ofNullable(typeMirror)
            .filter(tm -> TypeKind.DECLARED.equals(tm.getKind()))
            .map(TypeName::get)
            .flatMap(OptionalClassDetector::optionalDependencyTypeElement);
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
     * Detect if the given annotation exists, loading it if it does
     * @param className The class name to check
     * @return If it is known
     */
    public static boolean isAnnotationLoaded(final ClassName className) {
        return loadAnnotation(className).isPresent();
    }

    /**
     * Detect if the given annotation exists, loading it if it does
     * @param className The class name to check
     * @return If it is known
     */
    public static Optional<TypeElement> loadAnnotation(final ClassName className) {
        return LOADED_ANNOTATIONS.computeIfAbsent(className, OptionalClassDetector::loadAnnotation);
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
            return (TypeMirror returnType) -> eraseAndCompare(returnType, dependencyTypeMirror);
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
        return optionalDependencyTypeMirror(compareTo)
            .flatMap(
                compTo -> optionalDependencyTypeMirror(compareFrom)
                    .map(compFrom -> eraseAndCompare(compFrom, compTo))
            )
            .orElse(false);
    }

    private static synchronized boolean eraseAndCompare(TypeMirror compFrom, TypeMirror compTo) {
        final Types types = APContext.types();
        final TypeMirror erasedFrom = types.erasure(compFrom);
        final TypeMirror erasedTo = types.erasure(compTo);
        return types.isAssignable(erasedFrom, erasedTo) || types.isSubtype(erasedFrom, erasedTo);
    }
}

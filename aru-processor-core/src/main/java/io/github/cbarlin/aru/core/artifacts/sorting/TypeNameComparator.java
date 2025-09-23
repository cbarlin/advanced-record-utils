package io.github.cbarlin.aru.core.artifacts.sorting;

import io.micronaut.sourcegen.javapoet.TypeName;

import java.util.Comparator;

public final class TypeNameComparator {

    public static final Comparator<TypeName> INSTANCE = createComparator();

    private TypeNameComparator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Comparator<TypeName> createComparator() {
        return Comparator
            .comparing(TypeName::isPrimitive)
            .thenComparing(TypeName::isBoxedPrimitive)
            .thenComparing(TypeName::isAnnotated)
            .thenComparing(TypeName::toString);
    }
}

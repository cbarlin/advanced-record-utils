package io.github.cbarlin.aru.core.artifacts.sorting;

import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import java.util.Comparator;

public final class AnnotationSpecComparator {

    public static final Comparator<AnnotationSpec> INSTANCE = createComparator();

    private AnnotationSpecComparator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Comparator<AnnotationSpec> createComparator() {
        return Comparator
            .comparing((final AnnotationSpec a) -> a.type, TypeNameComparator.INSTANCE)
            .thenComparingInt((final AnnotationSpec a) -> a.toString().length());
    }
}

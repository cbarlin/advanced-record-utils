package io.github.cbarlin.aru.core.inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import io.micronaut.sourcegen.javapoet.ClassName;

public class Holder {

    private Holder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static List<ClassNameToPrismAdaptor> CLASS_NAME_PRISM_ADAPTORS;
    private static List<AnnotationInferencer> INFERENCERS;

    private static Map<ClassName, List<ClassNameToPrismAdaptor>> CN_PRISM_BY_CN = new HashMap<>();
    private static Map<ClassName, List<AnnotationInferencer>> INF_BY_CN = new HashMap<>();
    
    /**
     * Return adaptors that are designed for the given annotation class name
     */
    public static List<ClassNameToPrismAdaptor> adaptors(final ClassName annotationClassName) {
        return CN_PRISM_BY_CN.computeIfAbsent(
            annotationClassName,
            c -> adaptors().stream().filter(adaptor -> c.equals(adaptor.supportedAnnotationClassName())).toList()
        );
    }

    /**
     * Return inferencers that are designed for the given annotation class name
     */
    public static List<AnnotationInferencer> inferencers(final ClassName annotationClassName) {
        return INF_BY_CN.computeIfAbsent(
            annotationClassName, 
            c -> inferencers().stream().filter(inferencer -> c.equals(inferencer.supportedAnnotationClassName())).toList()
        );
    }

    /**
     * Return all adaptors known to the annotation processor
     * <p>
     * Note: You probably want {@link #adaptors(ClassName)} - it will cache lookups
     */
    public static List<ClassNameToPrismAdaptor> adaptors() {
        if (Objects.isNull(CLASS_NAME_PRISM_ADAPTORS)) {
            final List<ClassNameToPrismAdaptor> lst = new ArrayList<>();
            
            ServiceLoader.load(
                ClassNameToPrismAdaptor.class,
                ClassNameToPrismAdaptor.class.getClassLoader()
            )
            .forEach(lst::add);
            CLASS_NAME_PRISM_ADAPTORS = List.copyOf(lst);
        }
        return CLASS_NAME_PRISM_ADAPTORS;
    }

    /**
     * Return all inferencers known to the annotation processor
     * <p>
     * Note: You probably want {@link #inferencers(ClassName)} - it will cache lookups
     */
    public static List<AnnotationInferencer> inferencers() {
        if (Objects.isNull(INFERENCERS)) {
            final List<AnnotationInferencer> lst = new ArrayList<>();
            
            ServiceLoader.load(
                AnnotationInferencer.class,
                AnnotationInferencer.class.getClassLoader()
            )
            .forEach(lst::add);
            Collections.sort(lst);
            INFERENCERS = List.copyOf(lst);
        }
        return INFERENCERS;
    }
}

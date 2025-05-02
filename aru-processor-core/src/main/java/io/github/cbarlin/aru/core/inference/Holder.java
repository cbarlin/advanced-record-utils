package io.github.cbarlin.aru.core.inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

public class Holder {

    private static List<ClassNameToPrismAdaptor> CLASS_NAME_PRISM_ADAPTORS;
    private static List<AnnotationInferencer> INFERENCERS;

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

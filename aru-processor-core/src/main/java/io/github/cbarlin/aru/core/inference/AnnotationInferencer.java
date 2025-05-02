package io.github.cbarlin.aru.core.inference;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import io.avaje.spi.Service;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * A class that can "infer" a {@link TypeMirror} 
 */
@Service
public interface AnnotationInferencer extends Comparable<AnnotationInferencer> {

    /**
     * How specific is this inferrer?
     */
    public int specificity();

    /**
     * The class name of the annotation
     */
    public ClassName supportedAnnotationClassName();

    /**
     * Attempt to create a type mirror with values inferred from other items
     */
    public Optional<AnnotationMirror> inferAnnotationMirror(final Element element, final UtilsProcessingContext processingContext, final AdvancedRecordUtilsPrism prism);

    // We don't need to also override equals, as we won't be `equals` them since that doesn't make sense
    @SuppressWarnings({"java:S1210"})
    @Override
    default int compareTo(AnnotationInferencer o) {
        if (specificity() == o.specificity()) {
            return getClass().getName().compareTo(o.getClass().getName());
        }
        return Integer.compare(specificity(), o.specificity());
    }
}

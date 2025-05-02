package io.github.cbarlin.aru.core.inference;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import io.avaje.spi.Service;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * A service that can attempt to synthesise an {@link AnnotationMirror}
 */
@Service
public interface AnnotationInferencer extends Comparable<AnnotationInferencer> {

    /**
     * How specific is this inferrer?
     */
    int specificity();

    /**
     * The class name of the annotation
     */
    ClassName supportedAnnotationClassName();

    /**
     * Attempt to create a type mirror with values inferred from other items
     */
    Optional<AnnotationMirror> inferAnnotationMirror(final Element element, final UtilsProcessingContext processingContext, final AdvancedRecordUtilsPrism prism);

    // We don't need to also override equals, as we won't be `equals` them since that doesn't make sense
    @SuppressWarnings({"java:S1210"})
    @Override
    default int compareTo(AnnotationInferencer o) {
        if (specificity() == o.specificity()) {
            return getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
        }
        return Integer.compare(specificity(), o.specificity());
    }
}

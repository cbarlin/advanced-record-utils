package io.github.cbarlin.aru.core.inference;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import io.avaje.spi.Service;
import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * An adaptor that knows how to swap a type name for a prism
 */
@Service
public interface ClassNameToPrismAdaptor<T> {

    public ClassName supportedAnnotationClassName();

    public Class<T> prismClass();

    default ClassName supportedPrismClassName() {
        return ClassName.get(prismClass());
    }

    public Optional<T> optionalInstanceOf(final AnnotationMirror typeMirror);

    public Optional<T> optionalInstanceOn(final Element element);
}

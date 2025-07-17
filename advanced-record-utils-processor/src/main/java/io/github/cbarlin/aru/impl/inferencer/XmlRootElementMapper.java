package io.github.cbarlin.aru.impl.inferencer;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ROOT_ELEMENT;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;
import io.github.cbarlin.aru.prism.prison.XmlRootElementPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

@ServiceProvider
public final class XmlRootElementMapper implements ClassNameToPrismAdaptor<XmlRootElementPrism> {

    @Override
    public ClassName supportedAnnotationClassName() {
        return XML_ROOT_ELEMENT;
    }

    @Override
    public Class<XmlRootElementPrism> prismClass() {
        return XmlRootElementPrism.class;
    }

    @Override
    public Optional<XmlRootElementPrism> optionalInstanceOf(final AnnotationMirror typeMirror) {
        return XmlRootElementPrism.getOptional(typeMirror);
    }

    @Override
    public Optional<XmlRootElementPrism> optionalInstanceOn(final Element element) {
        return XmlRootElementPrism.getOptionalOn(element);
    }

}

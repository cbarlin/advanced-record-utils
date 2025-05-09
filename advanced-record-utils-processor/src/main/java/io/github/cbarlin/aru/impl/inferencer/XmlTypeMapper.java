package io.github.cbarlin.aru.impl.inferencer;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_TYPE;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;
import io.github.cbarlin.aru.prism.prison.XmlTypePrism;
import io.micronaut.sourcegen.javapoet.ClassName;

@ServiceProvider
public class XmlTypeMapper implements ClassNameToPrismAdaptor<XmlTypePrism> {

    @Override
    public ClassName supportedAnnotationClassName() {
        return XML_TYPE;
    }

    @Override
    public Class<XmlTypePrism> prismClass() {
        return XmlTypePrism.class;
    }

    @Override
    public Optional<XmlTypePrism> optionalInstanceOf(final AnnotationMirror typeMirror) {
        return XmlTypePrism.getOptional(typeMirror);
    }

    @Override
    public Optional<XmlTypePrism> optionalInstanceOn(final Element element) {
        return XmlTypePrism.getOptionalOn(element);
    }

}

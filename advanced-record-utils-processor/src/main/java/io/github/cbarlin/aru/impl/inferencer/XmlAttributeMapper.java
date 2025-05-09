package io.github.cbarlin.aru.impl.inferencer;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ATTRIBUTE;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.micronaut.sourcegen.javapoet.ClassName;

@ServiceProvider
public class XmlAttributeMapper implements ClassNameToPrismAdaptor<XmlAttributePrism> {

    @Override
    public ClassName supportedAnnotationClassName() {
        return XML_ATTRIBUTE;
    }

    @Override
    public Class<XmlAttributePrism> prismClass() {
        return XmlAttributePrism.class;
    }

    @Override
    public Optional<XmlAttributePrism> optionalInstanceOf(AnnotationMirror typeMirror) {
        return XmlAttributePrism.getOptional(typeMirror);
    }

    @Override
    public Optional<XmlAttributePrism> optionalInstanceOn(Element element) {
        if (element instanceof RecordComponentElement rce) {
            return XmlAttributePrism.getOptionalOn(rce.getAccessor())
                .or(() -> XmlAttributePrism.getOptionalOn(rce));
        }
        return XmlAttributePrism.getOptionalOn(element);
    }

}

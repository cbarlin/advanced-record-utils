package io.github.cbarlin.aru.impl.inferencer;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT_WRAPPER;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

@ServiceProvider
public class XmlElementWrapper implements ClassNameToPrismAdaptor<XmlElementWrapperPrism> {

    @Override
    public ClassName supportedAnnotationClassName() {
        return XML_ELEMENT_WRAPPER;
    }

    @Override
    public Class<XmlElementWrapperPrism> prismClass() {
        return XmlElementWrapperPrism.class;
    }

    @Override
    public Optional<XmlElementWrapperPrism> optionalInstanceOf(AnnotationMirror typeMirror) {
        return XmlElementWrapperPrism.getOptional(typeMirror);
    }

    @Override
    public Optional<XmlElementWrapperPrism> optionalInstanceOn(Element element) {
        if (element instanceof RecordComponentElement rce) {
            return XmlElementWrapperPrism.getOptionalOn(rce.getAccessor())
                .or(() -> XmlElementWrapperPrism.getOptionalOn(rce));
        }
        return XmlElementWrapperPrism.getOptionalOn(element);
    }

}

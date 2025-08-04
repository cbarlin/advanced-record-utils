package io.github.cbarlin.aru.impl.xml.inferencer;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import jakarta.inject.Singleton;

import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;
import java.util.Optional;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT_WRAPPER;

@Singleton
@GlobalScope
public final class XmlElementWrapperMapper {

    public Optional<XmlElementWrapperPrism> optionalInstanceOn(Element element) {
        if (!OptionalClassDetector.isAnnotationLoaded(XML_ELEMENT_WRAPPER)) {
            return Optional.empty();
        }
        if (element instanceof RecordComponentElement rce) {
            return XmlElementWrapperPrism.getOptionalOn(rce.getAccessor())
                .or(() -> XmlElementWrapperPrism.getOptionalOn(rce));
        }
        return XmlElementWrapperPrism.getOptionalOn(element);
    }

}

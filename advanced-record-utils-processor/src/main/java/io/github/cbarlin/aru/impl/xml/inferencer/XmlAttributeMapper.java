package io.github.cbarlin.aru.impl.xml.inferencer;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import jakarta.inject.Singleton;

import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;
import java.util.Optional;

@Singleton
@GlobalScope
public final class XmlAttributeMapper {

    public Optional<XmlAttributePrism> optionalInstanceOn(Element element) {
        if (element instanceof RecordComponentElement rce) {
            return OptionalClassDetector.loadAnnotation(Constants.Names.XML_ATTRIBUTE)
                    .flatMap(el -> XmlAttributePrism.getOptionalOn(rce.getAccessor()))
                    .or(() -> XmlAttributePrism.getOptionalOn(rce));
        }
        return OptionalClassDetector.loadAnnotation(Constants.Names.XML_ATTRIBUTE)
                .flatMap(el -> XmlAttributePrism.getOptionalOn(element));
    }

}

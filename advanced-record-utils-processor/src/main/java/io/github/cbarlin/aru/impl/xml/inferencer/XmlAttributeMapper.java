package io.github.cbarlin.aru.impl.xml.inferencer;

import io.github.cbarlin.aru.core.ExecutableElementUtils;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import jakarta.inject.Singleton;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import java.util.Optional;

@Singleton
@GlobalScope
public final class XmlAttributeMapper {

    public Optional<XmlAttributePrism> optionalInstanceOn(Element element) {
        if (element instanceof final RecordComponentElement rce) {
            return OptionalClassDetector.loadAnnotation(Constants.Names.XML_ATTRIBUTE)
                    .flatMap(el -> upCallingTree(rce.getAccessor()))
                    .or(() -> XmlAttributePrism.getOptionalOn(rce));
        } else if (element instanceof final ExecutableElement exe) {
            return OptionalClassDetector.loadAnnotation(Constants.Names.XML_ATTRIBUTE)
                .flatMap(el -> upCallingTree(exe));
        }
        return OptionalClassDetector.loadAnnotation(Constants.Names.XML_ATTRIBUTE)
                .flatMap(el -> XmlAttributePrism.getOptionalOn(element));
    }

    private Optional<XmlAttributePrism> upCallingTree(final ExecutableElement executableElement) {

        return XmlAttributePrism.getOptionalOn(executableElement)
            .or(
                () -> ExecutableElementUtils.obtainOverrideTree(executableElement)
                    .stream()
                    .map(XmlAttributePrism::getOptionalOn)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
            );
    }

}

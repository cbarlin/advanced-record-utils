package io.github.cbarlin.aru.impl.xml.inferencer;

import io.github.cbarlin.aru.core.ExecutableElementUtils;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import jakarta.inject.Singleton;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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
        if (element instanceof final RecordComponentElement rce) {
            return upCallingTree(rce.getAccessor())
                .or(() -> XmlElementWrapperPrism.getOptionalOn(rce));
        } else if (element instanceof final ExecutableElement exe) {
            return upCallingTree(exe);
        }
        return XmlElementWrapperPrism.getOptionalOn(element);
    }

    private Optional<XmlElementWrapperPrism> upCallingTree(final ExecutableElement executableElement) {

        return XmlElementWrapperPrism.getOptionalOn(executableElement)
            .or(
                () -> ExecutableElementUtils.obtainOverrideTree(executableElement)
                    .stream()
                    .map(XmlElementWrapperPrism::getOptionalOn)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
            );
    }

}

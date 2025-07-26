package io.github.cbarlin.aru.impl.xml.inferencer;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.mirrorhandlers.MapBasedAnnotationMirror;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.github.cbarlin.aru.prism.prison.JsonBIgnorePrism;
import io.github.cbarlin.aru.prism.prison.JsonIgnorePrism;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;
import jakarta.inject.Singleton;

import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;
import java.util.Map;
import java.util.Optional;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_TRANSIENT;

@Singleton
@GlobalScope
public final class XmlTransientMapper{

    public Optional<XmlTransientPrism> optionalInstanceOn(final Element element) {
        if (!OptionalClassDetector.isAnnotationLoaded(XML_TRANSIENT)) {
            return Optional.empty();
        }
        if (element instanceof final RecordComponentElement rce) {
            return XmlTransientPrism.getOptionalOn(rce.getAccessor())
                .or(() -> XmlTransientPrism.getOptionalOn(rce))
                .or(() -> inferAnnotationMirror(element));
        }
        return XmlTransientPrism.getOptionalOn(element)
            .or(() -> inferAnnotationMirror(element));
    }

    private static Optional<XmlTransientPrism> inferAnnotationMirror(final Element element) {
        if (element instanceof RecordComponentElement rce && hasIgnoreAnnotation(rce)) {
            return Optional.of(new MapBasedAnnotationMirror(XML_TRANSIENT, Map.of()))
                .map(XmlTransientPrism::getInstance);
        }
        return Optional.empty();
    }

    private static boolean hasIgnoreAnnotation(final RecordComponentElement rce) {
        return JsonBIgnorePrism.isPresent(rce) ||
            JsonBIgnorePrism.isPresent(rce.getAccessor()) ||
            JsonIgnorePrism.isPresent(rce) ||
            JsonIgnorePrism.isPresent(rce.getAccessor());
    }
}

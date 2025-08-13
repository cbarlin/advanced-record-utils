package io.github.cbarlin.aru.impl.xml.inferencer;

import io.github.cbarlin.aru.core.ExecutableElementUtils;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.mirrorhandlers.MapBasedAnnotationMirror;
import io.github.cbarlin.aru.impl.wiring.XmlPerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.JsonBPrism;
import io.github.cbarlin.aru.prism.prison.JsonBPropertyPrism;
import io.github.cbarlin.aru.prism.prison.JsonPropertyPrism;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementWrapperPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT;

@Singleton
@XmlPerRecordScope
public final class XmlElementMapper {

    private final AdvancedRecordUtilsPrism prism;

    public XmlElementMapper(final AdvancedRecordUtilsPrism prism) {
        this.prism = prism;
    }

    public Optional<XmlElementPrism> optionalInstanceOn(final Element element) {
        if (!OptionalClassDetector.isAnnotationLoaded(XML_ELEMENT)) {
            return Optional.empty();
        }
        if (element instanceof final RecordComponentElement rce) {
            return upCallingTree(rce.getAccessor())
                .or(() -> XmlElementPrism.getOptionalOn(element))
                .or(() -> inferAnnotationMirror(element));
        } else if (element instanceof final ExecutableElement exe) {
            return upCallingTree(exe);
        }
        return XmlElementPrism.getOptionalOn(element)
            .or(() -> inferAnnotationMirror(element));
    }

    private Optional<XmlElementPrism> upCallingTree(final ExecutableElement executableElement) {

        return XmlElementPrism.getOptionalOn(executableElement)
            .or(
                () -> ExecutableElementUtils.obtainOverrideTree(executableElement)
                    .stream()
                    .map(XmlElementPrism::getOptionalOn)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
            );
    }


    private Optional<XmlElementPrism> inferAnnotationMirror(final Element element) {
        if (element instanceof RecordComponentElement rce) {
            if (Boolean.TRUE.equals(prism.addJsonbImportAnnotation()) || JsonBPrism.isPresent(rce.getEnclosingElement())) {
                // Nice!
                return JsonBPropertyPrism.getOptionalOn(rce)
                    .or(() -> JsonBPropertyPrism.getOptionalOn(rce.getAccessor()))
                    .map(propPrism -> new MapBasedAnnotationMirror(XML_ELEMENT, Map.of("name", propPrism.value())))
                    .or(() -> extractInferredName(rce, prism).map(mp -> new MapBasedAnnotationMirror(XML_ELEMENT, mp)))
                    .map(XmlElementPrism::getInstance);
            } else if (JsonPropertyPrism.isPresent(element) || JsonPropertyPrism.isPresent(rce.getAccessor())) {
                final var vals = JsonPropertyPrism.getOptionalOn(rce.getAccessor())
                    .or(() -> JsonPropertyPrism.getOptionalOn(rce))
                    .map(XmlElementMapper::extractJacksonProperty);
                return Optional.of(new MapBasedAnnotationMirror(XML_ELEMENT, vals))
                    .map(XmlElementPrism::getInstance);
            }
            return extractInferredName(rce, prism)
                .map(mp -> new MapBasedAnnotationMirror(XML_ELEMENT, mp))
                .map(XmlElementPrism::getInstance);
        }
        return Optional.empty();
    }

    private static Optional<Map<String, Object>> extractInferredName(final RecordComponentElement element, final AdvancedRecordUtilsPrism prism) {
        if (XmlAttributePrism.isPresent(element.getAccessor()) || XmlTransientPrism.isPresent(element.getAccessor()) || XmlElementsPrism.isPresent(element.getAccessor()) || XmlAttributePrism.isPresent(element) || XmlTransientPrism.isPresent(element) || XmlElementsPrism.isPresent(element)) {
            return Optional.empty();
        } else {
            return switch (prism.xmlOptions().inferXmlElementName()) {
                case "MATCH" -> Optional.of(Map.of("name", element.getSimpleName().toString()));
                case "UPPER_FIRST_LETTER" -> Optional.of(Map.of("name", capitalise(element.getSimpleName().toString())));
                case null, default -> Optional.empty();
            };
        }
    }

    private static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }

    private static Map<String, Object> extractJacksonProperty(JsonPropertyPrism propPrism) {
        final Map<String, Object> vs = new HashMap<>();

        if (StringUtils.isNotBlank(propPrism.value())) {
            vs.put("name", propPrism.value());
        }
        if (StringUtils.isNotBlank(propPrism.namespace())) {
            vs.put("namespace", propPrism.namespace());
        }
        if (StringUtils.isNotBlank(propPrism.defaultValue())) {
            vs.put("defaultValue", propPrism.defaultValue());
        }
        if (Objects.nonNull(propPrism.required())) {
            vs.put("required", propPrism.required());
        }

        return vs;
    }

}

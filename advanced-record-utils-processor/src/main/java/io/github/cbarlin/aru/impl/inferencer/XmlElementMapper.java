package io.github.cbarlin.aru.impl.inferencer;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;

import org.apache.commons.lang3.StringUtils;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.inference.AnnotationInferencer;
import io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;
import io.github.cbarlin.aru.core.mirrorhandlers.MapBasedAnnotationMirror;
import io.github.cbarlin.aru.prism.prison.JsonBPrism;
import io.github.cbarlin.aru.prism.prison.JsonBPropertyPrism;
import io.github.cbarlin.aru.prism.prison.JsonPropertyPrism;
import io.github.cbarlin.aru.prism.prison.XmlAttributePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

// Unless manually specified like this, the Avaje SPI only picks up the `ClassNameToPrismAdaptor`
@ServiceProvider({ClassNameToPrismAdaptor.class, AnnotationInferencer.class})
public final class XmlElementMapper implements ClassNameToPrismAdaptor<XmlElementPrism>, AnnotationInferencer {

    @Override
    public ClassName supportedAnnotationClassName() {
        return XML_ELEMENT;
    }

    @Override
    public Class<XmlElementPrism> prismClass() {
        return XmlElementPrism.class;
    }

    @Override
    public Optional<XmlElementPrism> optionalInstanceOf(final AnnotationMirror typeMirror) {
        return XmlElementPrism.getOptional(typeMirror);
    }

    @Override
    public Optional<XmlElementPrism> optionalInstanceOn(final Element element) {
        if (element instanceof final RecordComponentElement rce) {
            return XmlElementPrism.getOptionalOn(rce.getAccessor())
                .or(() -> XmlElementPrism.getOptionalOn(element));
        }
        return XmlElementPrism.getOptionalOn(element);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    public Optional<AnnotationMirror> inferAnnotationMirror(
            final Element element, 
            final UtilsProcessingContext processingContext,
            final AdvancedRecordUtilsPrism prism
    ) {
        if (element instanceof RecordComponentElement rce) {
            if (XmlAttributePrism.isPresent(rce) || XmlTransientPrism.isPresent(rce) || XmlAttributePrism.isPresent(rce.getAccessor()) || XmlTransientPrism.isPresent(rce.getAccessor())) {
                return Optional.empty();
            } else if (Boolean.TRUE.equals(prism.addJsonbImportAnnotation()) || JsonBPrism.isPresent(rce.getEnclosingElement())) {
                // Nice!
                return JsonBPropertyPrism.getOptionalOn(rce)
                    .or(() -> JsonBPropertyPrism.getOptionalOn(rce.getAccessor()))
                    .map(propPrism -> new MapBasedAnnotationMirror(XML_ELEMENT, Map.of("name", propPrism.value())))
                    .or(() -> Optional.ofNullable(new MapBasedAnnotationMirror(XML_ELEMENT, extractInferredName(rce, prism))))
                    .map(AnnotationMirror.class::cast);
            } else if (JsonPropertyPrism.isPresent(element) || JsonPropertyPrism.isPresent(rce.getAccessor())) {
                final var vals = JsonPropertyPrism.getOptionalOn(rce.getAccessor())
                    .or(() -> JsonPropertyPrism.getOptionalOn(rce))
                    .map(XmlElementMapper::extractJacksonProperty);
                return Optional.of(new MapBasedAnnotationMirror(XML_ELEMENT, vals));
            }
            return extractInferredName(rce, prism)
                .map(mp -> new MapBasedAnnotationMirror(XML_ELEMENT, mp));
        }
        return Optional.empty();
    }

    private static Optional<Map<String, Object>> extractInferredName(final RecordComponentElement element, final AdvancedRecordUtilsPrism prism) {
        if (XmlAttributePrism.isPresent(element) || XmlTransientPrism.isPresent(element) || XmlElementsPrism.isPresent(element)) {
            return Optional.empty();
        }
        if (XmlAttributePrism.isPresent(element.getAccessor()) || XmlTransientPrism.isPresent(element.getAccessor()) || XmlElementsPrism.isPresent(element.getAccessor())) {
            return Optional.empty();
        }
        return switch (prism.xmlOptions().inferXmlElementName()) {
            case "MATCH" -> Optional.of(Map.of("name", element.getSimpleName().toString()));
            case "UPPER_FIRST_LETTER" -> Optional.of(Map.of("name", capitalise(element.getSimpleName().toString())));
            case null, default -> Optional.empty();
        };
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

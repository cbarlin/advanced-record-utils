package io.github.cbarlin.aru.impl.inferencer;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT;

import java.util.HashMap;
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
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

@ServiceProvider
public class XmlElementMapper implements ClassNameToPrismAdaptor<XmlElementPrism>, AnnotationInferencer {

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
            if (Boolean.TRUE.equals(prism.addJsonbImportAnnotation()) || JsonBPrism.isPresent(element.getEnclosingElement())) {
                // Nice!
                return JsonBPropertyPrism.getOptionalOn(rce)
                    .or(() -> JsonBPropertyPrism.getOptionalOn(rce.getAccessor()))
                    .map(propPrism -> new MapBasedAnnotationMirror(XML_ELEMENT, Map.of("name", propPrism.value())))
                    .or(() -> Optional.ofNullable(new MapBasedAnnotationMirror(XML_ELEMENT)))
                    .map(AnnotationMirror.class::cast);
            } else if (JsonPropertyPrism.isPresent(element) || JsonPropertyPrism.isPresent(rce.getAccessor())) {
                final var vals = JsonPropertyPrism.getOptionalOn(rce.getAccessor())
                    .or(() -> JsonPropertyPrism.getOptionalOn(rce))
                    .map(XmlElementMapper::extractJacksonProperty);
                return Optional.of(new MapBasedAnnotationMirror(XML_ELEMENT, vals));
            }
        }
        return Optional.empty();
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

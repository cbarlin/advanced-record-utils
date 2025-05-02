package io.github.cbarlin.aru.impl.inferencer;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_TRANSIENT;

import java.util.Map;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.inference.AnnotationInferencer;
import io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;
import io.github.cbarlin.aru.core.mirrorhandlers.MapBasedAnnotationMirror;
import io.github.cbarlin.aru.prism.prison.JsonBIgnorePrism;
import io.github.cbarlin.aru.prism.prison.JsonIgnorePrism;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

@ServiceProvider
public class XmlTransientMapper implements ClassNameToPrismAdaptor<XmlTransientPrism>, AnnotationInferencer {

    @Override
    public ClassName supportedAnnotationClassName() {
        return XML_TRANSIENT;
    }

    @Override
    public Class<XmlTransientPrism> prismClass() {
        return XmlTransientPrism.class;
    }

    @Override
    public Optional<XmlTransientPrism> optionalInstanceOf(final AnnotationMirror typeMirror) {
        return XmlTransientPrism.getOptional(typeMirror);
    }

    @Override
    public Optional<XmlTransientPrism> optionalInstanceOn(final Element element) {
        if (element instanceof final RecordComponentElement rce) {
            return XmlTransientPrism.getOptionalOn(rce.getAccessor())
                .or(() -> XmlTransientPrism.getOptionalOn(element));
        }
        return XmlTransientPrism.getOptionalOn(element);
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
        if (element instanceof RecordComponentElement rce && hasIgnoreAnnotation(rce)) {
            return Optional.of(new MapBasedAnnotationMirror(XML_TRANSIENT, Map.of()));
        }
        return Optional.empty();
    }

    private boolean hasIgnoreAnnotation(final RecordComponentElement rce) {
        return JsonBIgnorePrism.isPresent(rce) ||
            JsonBIgnorePrism.isPresent(rce.getAccessor()) ||
            JsonIgnorePrism.isPresent(rce) ||
            JsonIgnorePrism.isPresent(rce.getAccessor());
    }
}

package io.github.cbarlin.aru.impl.inferencer;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENTS;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.inference.AnnotationInferencer;
import io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;
import io.github.cbarlin.aru.core.mirrorhandlers.MapBasedAnnotationMirror;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

public class XmlElementsMapper implements ClassNameToPrismAdaptor<XmlElementsPrism>, AnnotationInferencer {

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    public Optional<AnnotationMirror> inferAnnotationMirror(
            final Element element, 
            final UtilsProcessingContext processingContext,
            final AdvancedRecordUtilsPrism prism
    ) {
        if (element instanceof TypeElement te && processingContext.analysedType(te) instanceof AnalysedInterface ai) {
            final List<AnnotationMirror> mirrors = List.of();
            ai.implementingTypes()
                .forEach(pt -> mirrors.add(new MapBasedAnnotationMirror(
                    XML_ELEMENT,
                    Map.of("type", pt.utilsClassName())
                )));
            return Optional.of(new MapBasedAnnotationMirror(XML_ELEMENTS, Map.of("value", mirrors)));
        }
        return Optional.empty();    
    }

    @Override
    public ClassName supportedAnnotationClassName() {
        return XML_ELEMENTS;
    }

    @Override
    public Class<XmlElementsPrism> prismClass() {
        return XmlElementsPrism.class;
    }

    @Override
    public Optional<XmlElementsPrism> optionalInstanceOf(AnnotationMirror typeMirror) {
        return XmlElementsPrism.getOptional(typeMirror);
    }

    @Override
    public Optional<XmlElementsPrism> optionalInstanceOn(Element element) {
        return XmlElementsPrism.getOptionalOn(element);
    }

}

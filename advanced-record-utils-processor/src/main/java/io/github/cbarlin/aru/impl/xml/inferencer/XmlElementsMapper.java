package io.github.cbarlin.aru.impl.xml.inferencer;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.mirrorhandlers.MapBasedAnnotationMirror;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.github.cbarlin.aru.prism.prison.XmlSeeAlsoPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENTS;

@Singleton
@GlobalScope
public final class XmlElementsMapper {

    private final UtilsProcessingContext processingContext;

    public XmlElementsMapper(final UtilsProcessingContext processingContext) {
        OptionalClassDetector.loadAnnotation(XML_ELEMENTS);
        this.processingContext = processingContext;
    }

    public Optional<XmlElementsPrism> optionalInstanceOn(Element element) {
        if (element instanceof final RecordComponentElement rce) {
            return XmlElementsPrism.getOptionalOn(rce.getAccessor())
                .or(() -> XmlElementsPrism.getOptionalOn(element));
        }
        return XmlElementsPrism.getOptionalOn(element);
    }

    private Optional<XmlElementsPrism> inferAnnotationMirror(final TypeMirror typeMirror) {
        return Optional.ofNullable(APContext.asTypeElement(typeMirror))
            .flatMap(this::inferAnnotationMirror);
    }

    public Optional<XmlElementsPrism> inferAnnotationMirror(final Element element) {
        if (element instanceof TypeElement te) {
            if (TypeName.get(te.asType()) instanceof ParameterizedTypeName ptn) {
                Optional.of(inferMirror(ptn))
                    .filter(Predicate.not(List::isEmpty))
                    .map(annotationMirrors -> new MapBasedAnnotationMirror(XML_ELEMENTS, Map.of("value", annotationMirrors)))
                    .map(XmlElementsPrism::getInstance);
            }
            return Optional.of(extractFromConcreteTypeElement(te))
                .filter(Predicate.not(List::isEmpty))
                .map(annotationMirrors -> new MapBasedAnnotationMirror(XML_ELEMENTS, Map.of("value", annotationMirrors)))
                .map(XmlElementsPrism::getInstance);

        } else if (element instanceof RecordComponentElement recordComponentElement) {
            return this.inferAnnotationMirror(recordComponentElement.asType());
        }
        return Optional.empty();
    }

    private List<AnnotationMirror> inferMirror(final TypeName typeName) {
        if (typeName instanceof ClassName cn) {
            return Optional.ofNullable(APContext.elements().getTypeElement(cn.canonicalName()))
                .map(this::extractFromConcreteTypeElement)
                .orElse(List.of());
        } else if (typeName instanceof ParameterizedTypeName ptn) {
            final List<AnnotationMirror> mirrors = new ArrayList<>();
            mirrors.addAll(inferMirror(ptn.rawType));
            ptn.typeArguments.forEach(arg -> mirrors.addAll(inferMirror(arg)));
            return List.copyOf(mirrors);
        }
        return List.of();
    }

    private List<AnnotationMirror> extractFromConcreteTypeElement(final TypeElement te) {
        final List<AnnotationMirror> annotationMirrors = new ArrayList<>();
        final Set<TypeMirror> typeMirrors = new HashSet<>();
        if (processingContext.analysedType(te) instanceof AnalysedInterface ai) {
            ai.implementingTypes().stream()
                .map(ProcessingTarget::typeElement)
                .map(TypeElement::asType)
                .filter(pt -> typeMirrors.add(pt))
                .map(TypeName::get)
                .forEach(pt -> annotationMirrors.add(
                    new MapBasedAnnotationMirror(XML_ELEMENT, Map.of("type", pt))
                ));
        }
        XmlSeeAlsoPrism.getOptionalOn(te)
            .ifPresent(prism -> prism.value().stream()
                .filter(typeMirrors::add)
                .map(TypeName::get)
                .forEach(pt -> annotationMirrors.add(
                    new MapBasedAnnotationMirror(XML_ELEMENT, Map.of("type", pt))
                )));
        
        return List.copyOf(annotationMirrors);
    }
}

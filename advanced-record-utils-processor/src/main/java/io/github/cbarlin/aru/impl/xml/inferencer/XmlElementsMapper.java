package io.github.cbarlin.aru.impl.xml.inferencer;

import io.github.cbarlin.aru.core.ExecutableElementUtils;
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
import javax.lang.model.element.ExecutableElement;
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

import static io.github.cbarlin.aru.core.CommonsConstants.Names.XML_SEE_ALSO;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENT;
import static io.github.cbarlin.aru.impl.Constants.Names.XML_ELEMENTS;

@Singleton
@GlobalScope
public final class XmlElementsMapper {

    private final UtilsProcessingContext processingContext;

    public XmlElementsMapper(final UtilsProcessingContext processingContext) {
        OptionalClassDetector.loadAnnotation(XML_ELEMENTS);
        OptionalClassDetector.loadAnnotation(XML_SEE_ALSO);
        this.processingContext = processingContext;
    }

    public Optional<XmlElementsPrism> optionalInstanceOn(final Element element) {
        if (element instanceof final RecordComponentElement rce) {
            return upCallingTree(rce.getAccessor()).or(() -> XmlElementsPrism.getOptionalOn(element));
        } else if (element instanceof ExecutableElement exe) {
            return upCallingTree(exe);
        }
        return XmlElementsPrism.getOptionalOn(element);
    }

    private Optional<XmlElementsPrism> upCallingTree(final ExecutableElement executableElement) {

        return XmlElementsPrism.getOptionalOn(executableElement)
            .or(
                () -> ExecutableElementUtils.obtainOverrideTree(executableElement)
                    .stream()
                    .map(XmlElementsPrism::getOptionalOn)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
            );
    }

    private Optional<XmlElementsPrism> inferAnnotationMirror(final TypeMirror typeMirror) {
        return OptionalClassDetector.optionalDependencyTypeElement(typeMirror)
            .flatMap(this::inferAnnotationMirror);
    }

    public Optional<XmlElementsPrism> inferAnnotationMirror(final Element element) {
        return switch (element) {
            case RecordComponentElement rce -> upCallingTree(rce.getAccessor())
                .or(() -> XmlElementsPrism.getOptionalOn(element))
                .or(() -> inferAnnotationMirror(rce.asType()));
            case ExecutableElement exe -> upCallingTree(exe);
            case TypeElement te when TypeName.get(te.asType()) instanceof ParameterizedTypeName ptn -> Optional.of(inferMirror(ptn))
                .filter(Predicate.not(List::isEmpty))
                .map(annotationMirrors -> new MapBasedAnnotationMirror(XML_ELEMENTS, Map.of("value", annotationMirrors)))
                .flatMap(XmlElementsPrism::getOptional);
            case TypeElement te -> Optional.of(extractFromConcreteTypeElement(te))
                .filter(Predicate.not(List::isEmpty))
                .map(annotationMirrors -> new MapBasedAnnotationMirror(XML_ELEMENTS, Map.of("value", annotationMirrors)))
                .flatMap(XmlElementsPrism::getOptional);
            case null, default -> Optional.empty();
        };
    }

    private List<AnnotationMirror> inferMirror(final TypeName typeName) {
        if (typeName instanceof ClassName cn) {
            return OptionalClassDetector.optionalDependencyTypeElement(cn)
                .map(this::extractFromConcreteTypeElement)
                .orElse(List.of());
        } else if (typeName instanceof ParameterizedTypeName ptn) {
            final List<AnnotationMirror> mirrors = new ArrayList<>(inferMirror(ptn.rawType));
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
                .filter(typeMirrors::add)
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

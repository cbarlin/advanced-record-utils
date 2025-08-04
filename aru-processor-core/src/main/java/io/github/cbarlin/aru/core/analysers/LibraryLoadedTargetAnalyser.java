package io.github.cbarlin.aru.core.analysers;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsGeneratedPrism;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Priority(1)
@CoreGlobalScope
public final class LibraryLoadedTargetAnalyser implements TargetAnalyser {

    private final TypeConverterAnalyser typeConverterAnalyser;

    public LibraryLoadedTargetAnalyser(TypeConverterAnalyser typeConverterAnalyser) {
        this.typeConverterAnalyser = typeConverterAnalyser;
    }

    @Override
    public TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings) {
        if (!OptionalClassDetector.isAnnotationLoaded(CommonsConstants.Names.ARU_GENERATED)) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        if ((element instanceof final TypeElement typeElement) && ElementKind.CLASS.equals(typeElement.getKind()) && AdvancedRecordUtilsGeneratedPrism.isPresent(typeElement)) {
            // Excellent, I have a target!
            final PreBuilt preBuilt = new PreBuilt(typeElement);
            final AdvancedRecordUtilsGeneratedPrism prism = AdvancedRecordUtilsGeneratedPrism.getInstanceOn(typeElement);
            if (Objects.isNull(prism)) {
                return TargetAnalysisResult.EMPTY_RESULT;
            }
            final Element targetEl = APContext.types().asElement(prism.generatedFor());
            if (!(targetEl instanceof final TypeElement target)) {
                return TargetAnalysisResult.EMPTY_RESULT;
            }
            final Set<TypeElement> references = prism.references()
                .stream()
                .map(APContext.types()::asElement)
                .filter(Objects::nonNull)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .collect(Collectors.toUnmodifiableSet());
            final LibraryLoadedTarget libraryLoadedTarget = new LibraryLoadedTarget(preBuilt, target);
            // Oof!
            final Set<AnalysedTypeConverter> foundConverter = findConverters(prism);

            return new TargetAnalysisResult(Optional.of(libraryLoadedTarget), references, true, foundConverter);
        }

        return TargetAnalysisResult.EMPTY_RESULT;
    }

    private Set<AnalysedTypeConverter> findConverters(final AdvancedRecordUtilsGeneratedPrism prism) {
        final Set<AnalysedTypeConverter> converters = HashSet.newHashSet(1);
        for (final TypeMirror usedTypeConverter : prism.usedTypeConverters()) {
            final var te = OptionalClassDetector.optionalDependencyTypeElement(usedTypeConverter);
            if (te.isPresent()) {
                final TypeElement typeElement = te.get();
                for (final ExecutableElement enclosedElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    converters.addAll(typeConverterAnalyser.analyse(enclosedElement).foundConverter());
                }
            }
        }
        return Set.copyOf(converters);
    }
}

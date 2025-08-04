package io.github.cbarlin.aru.core.analysers;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsGeneratedPrism;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Priority(1)
@CoreGlobalScope
public final class LibraryLoadedTargetAnalyser implements TargetAnalyser {

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
            return new TargetAnalysisResult(Optional.of(libraryLoadedTarget), references, true, Optional.empty());
        }

        return TargetAnalysisResult.EMPTY_RESULT;
    }
}

package io.github.cbarlin.aru.core.analysers;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.wiring.AruGlobal;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsGeneratedPrism;

@Component
@AruGlobal
@Priority(1)
public final class LibraryLoadedTargetAnalyser implements TargetAnalyser {

    @Override
    public TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings) {
        if ((element instanceof final TypeElement typeElement) && ElementKind.CLASS.equals(typeElement.getKind()) && AdvancedRecordUtilsGeneratedPrism.isPresent(typeElement)) {
            // Excellent, I have a target!
            final PreBuilt preBuilt = new PreBuilt(typeElement);
            final LibraryLoadedTarget libraryLoadedTarget = new LibraryLoadedTarget(preBuilt, typeElement);
            final Set<TypeElement> references = AdvancedRecordUtilsGeneratedPrism.getInstanceOn(typeElement)
                .references()
                .stream()
                .map(tm -> APContext.asTypeElement(tm))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
            return new TargetAnalysisResult(Optional.of(libraryLoadedTarget), references, true);
        }

        return TargetAnalysisResult.EMPTY_RESULT;
    }
}

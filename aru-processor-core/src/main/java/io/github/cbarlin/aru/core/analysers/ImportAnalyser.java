package io.github.cbarlin.aru.core.analysers;

import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.github.cbarlin.aru.prism.prison.ImportLibraryUtilsPrism;
import jakarta.inject.Singleton;

import javax.lang.model.element.Element;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Priority(-1)
@CoreGlobalScope
public final class ImportAnalyser implements TargetAnalyser {

    @Override
    public TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings) {
        if (!OptionalClassDetector.isAnnotationLoaded(CommonsConstants.Names.ARU_IMPORT_LIBRARY)) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        return ImportLibraryUtilsPrism.getOptionalOn(element)
            .map(ImportLibraryUtilsPrism::value)
            .map(
                targets -> targets
                    .stream()
                    .map(OptionalClassDetector::optionalDependencyTypeElement)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toUnmodifiableSet())
            )
            .map(found -> new TargetAnalysisResult(Optional.empty(), found, false, Set.of()))
            .orElse(TargetAnalysisResult.EMPTY_RESULT);
    }

}

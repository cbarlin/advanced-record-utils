package io.github.cbarlin.aru.core.analysers;

import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.types.ProcessingTarget;

import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;

public record TargetAnalysisResult (
        Optional<ProcessingTarget> target,
        Set<TypeElement> foundElements,
        boolean isRootElement,
        Set<AnalysedTypeConverter> foundConverter
) {
    public static final TargetAnalysisResult EMPTY_RESULT = new TargetAnalysisResult(Optional.empty(), Set.of(), false, Set.of());
}

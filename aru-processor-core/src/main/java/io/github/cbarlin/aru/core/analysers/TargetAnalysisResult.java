package io.github.cbarlin.aru.core.analysers;

import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import io.github.cbarlin.aru.core.types.ProcessingTarget;

public record TargetAnalysisResult(
    Optional<ProcessingTarget> target,
    Set<TypeElement> foundElements,
    boolean isRootElement
) {
    public static final TargetAnalysisResult EMPTY_RESULT = new TargetAnalysisResult(Optional.empty(), Set.of(), false);
}

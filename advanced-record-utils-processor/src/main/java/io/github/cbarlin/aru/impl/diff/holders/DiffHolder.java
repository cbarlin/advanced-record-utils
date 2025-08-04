package io.github.cbarlin.aru.impl.diff.holders;

import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.misc.MatchingInterface;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.github.cbarlin.aru.prism.prison.DiffOptionsPrism;

public record DiffHolder(
    DiffInterfaceClass interfaceClass,
    DiffResultsClass resultsClass,
    DiffStaticClass staticClass,
    DiffOptionsPrism diffOptionsPrism,
    BuilderOptionsPrism builderOptionsPrism,
    MatchingInterface matchingInterface,
    AnalysedRecord analysedRecord
) {
}

package io.github.cbarlin.aru.impl.merger;

import java.util.Set;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.misc.MatchingInterface;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.github.cbarlin.aru.prism.prison.MergerOptionsPrism;

public record MergerHolder(
    ToBeBuilt mergerInterface,
    ToBeBuilt mergerStaticClass,
    BuilderOptionsPrism builderOptionsPrism,
    MergerOptionsPrism mergerOptionsPrism,
    MatchingInterface matchingInterface,
    AnalysedRecord analysedRecord,
    Set<String> processedMethods
) {

}

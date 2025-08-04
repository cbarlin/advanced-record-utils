package io.github.cbarlin.aru.impl.externs;

import io.avaje.inject.Component;
import io.avaje.inject.External;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.wiring.BasePerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;

@Component
@BasePerRecordScope
public record ExternalFromRecord(
    @External AdvRecUtilsSettings settings,
    @External BuilderClass builderClass,
    @External UtilsClass utilsClass,
    @External AnalysedRecord analysedRecord,
    @External AdvancedRecordUtilsPrism prism,
    @External BuilderOptionsPrism builderOptionsPrism
) {
}

package io.github.cbarlin.aru.impl.externs;

import io.avaje.inject.Component;
import io.avaje.inject.External;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.impl.wiring.BasePerInterfaceScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;

@Component
@BasePerInterfaceScope
public record ExternalFromInterface(
    @External AdvRecUtilsSettings settings,
    @External UtilsClass utilsClass,
    @External AnalysedInterface analysedRecord,
    @External AdvancedRecordUtilsPrism prism
) {

}

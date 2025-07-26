package io.github.cbarlin.aru.core.factories;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.wiring.CorePerInterfaceScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;

@Factory
@CorePerInterfaceScope
public final class InterfaceElementPrismFactory {

    @Bean
    @BeanTypes(UtilsClass.class)
    UtilsClass utilsClass(final AnalysedInterface analysedType) {
        return new UtilsClass(analysedType.utilsClass());
    }

    @Bean
    AdvRecUtilsSettings settingsPerRecord(final AnalysedInterface analysedType) {
        return analysedType.settings();
    }

    @Bean
    AdvancedRecordUtilsPrism rootPrism(final AdvRecUtilsSettings settings) {
        return settings.prism();
    }
}

package io.github.cbarlin.aru.core.meta;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Profile;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;

@Factory
public final class UtilsPrismFactory {

    @Bean
    @Profile({"aru-reset-per-record", "aru-reset-per-interface"})
    AdvancedRecordUtilsPrism rootPrism(final AdvRecUtilsSettings settings) {
        return settings.prism();
    }
}

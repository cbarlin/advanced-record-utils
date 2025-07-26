package io.github.cbarlin.aru.core.meta;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Profile;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.types.AnalysedType;

@Factory
public final class SettingsFactory {

    @Bean
    @Profile({"aru-reset-per-record", "aru-reset-per-interface"})
    AdvRecUtilsSettings settingsPerRecord(final AnalysedType analysedType) {
        return analysedType.settings();
    }

}

package io.github.cbarlin.aru.core.meta;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.wiring.ResetPerRecord;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;

@Factory
public final class BuilderPrismFactory {
    
    @Bean
    @ResetPerRecord
    BuilderOptionsPrism builderPrism(final AdvancedRecordUtilsPrism prism) {
        return prism.builderOptions();
    }
}

package io.github.cbarlin.aru.core.meta;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import io.avaje.inject.Profile;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.ProcessingTarget;

@Factory
public final class AnalysedTypeFactory {

    @Bean
    @BeanTypes(AnalysedType.class)
    @Profile({"aru-reset-per-record", "aru-reset-per-interface"})
    AnalysedType obtainAnalysedTypePerInterface(final @External ProcessingTarget processingTarget) {
        if (processingTarget instanceof AnalysedType analysedType) {
            return analysedType;
        }
        return null;
    }
}

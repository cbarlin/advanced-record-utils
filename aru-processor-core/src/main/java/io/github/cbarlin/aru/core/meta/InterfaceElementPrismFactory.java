package io.github.cbarlin.aru.core.meta;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.wiring.ResetPerInterface;

@Factory
public final class InterfaceElementPrismFactory {

    @Bean
    @ResetPerInterface
    AnalysedInterface obtainInterface(final AnalysedType analysedType) {
        if (analysedType instanceof final AnalysedInterface ai) {
            return ai;
        }
        return null;
    }
}

package io.github.cbarlin.aru.core.meta;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.wiring.ResetPerRecord;

@Factory
public final class RecordElementPrismFactory {

    @Bean
    @ResetPerRecord
    @BeanTypes(AnalysedRecord.class)
    AnalysedRecord obtainRecord(final AnalysedType analysedType) {
        if (analysedType instanceof final AnalysedRecord ar) {
            return ar;
        }
        return null;
    }
}

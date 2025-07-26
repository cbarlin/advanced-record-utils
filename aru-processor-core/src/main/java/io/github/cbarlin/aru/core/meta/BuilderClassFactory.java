package io.github.cbarlin.aru.core.meta;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.wiring.ResetPerRecord;

@Factory
public final class BuilderClassFactory {

    @Bean
    @ResetPerRecord
    @BeanTypes(BuilderClass.class)
    BuilderClass builderClass(final AnalysedRecord analysedRecord) {
        return new BuilderClass(analysedRecord.builderArtifact());
    }
}

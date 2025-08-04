package io.github.cbarlin.aru.core.factories;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;

@Factory
@CorePerRecordScope
public final class RecordElementPrismFactory {

    @Bean
    @BeanTypes(UtilsClass.class)
    UtilsClass utilsClass(final AnalysedRecord analysedType) {
        return new UtilsClass(analysedType.utilsClass());
    }

    @Bean
    @BeanTypes(BuilderClass.class)
    BuilderClass builderClass(final AnalysedRecord analysedRecord) {
        return new BuilderClass(analysedRecord.builderArtifact());
    }

    @Bean
    AdvRecUtilsSettings settingsPerRecord(final AnalysedRecord analysedType) {
        return analysedType.settings();
    }

    @Bean
    AdvancedRecordUtilsPrism rootPrism(final AdvRecUtilsSettings settings) {
        return settings.prism();
    }

    @Bean
    BuilderOptionsPrism builderPrism(final AdvancedRecordUtilsPrism prism) {
        return prism.builderOptions();
    }
}

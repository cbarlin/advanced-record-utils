package io.github.cbarlin.aru.core.impl.types.analyser;

import javax.lang.model.element.RecordComponentElement;

import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ComponentAnalyser;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class BasicAnalyser implements ComponentAnalyser {

    @Override
    public int specificity() {
        // This should be the "last resort", although it'll probably
        //   be chosen fairly often!
        return Integer.MIN_VALUE;
    }

    @Override
    public @Nullable AnalysedComponent analyseComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord,
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        return new AnalysedComponent(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
    }

}

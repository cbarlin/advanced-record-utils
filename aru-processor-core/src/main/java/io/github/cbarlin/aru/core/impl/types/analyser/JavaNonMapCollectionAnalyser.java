package io.github.cbarlin.aru.core.impl.types.analyser;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;

import javax.lang.model.element.RecordComponentElement;

import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ComponentAnalyser;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class JavaNonMapCollectionAnalyser implements ComponentAnalyser {

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    public @Nullable AnalysedComponent analyseComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord,
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        if (OptionalClassDetector.checkSameOrSubType(element, COLLECTION)) {
            return new AnalysedCollectionComponent(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        }
        return null;
    }

}

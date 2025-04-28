package io.github.cbarlin.aru.impl.types.dependencies;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__RICH_ITERABLE;

import javax.lang.model.element.RecordComponentElement;

import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ComponentAnalyser;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class EclipseNonMapComponentAnalyser implements ComponentAnalyser {

    @Override
    public int specificity() {
        return 2;
    }

    @Override
    public @Nullable AnalysedComponent analyseComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord,
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        if (OptionalClassDetector.checkSameOrSubType(element, ECLIPSE_COLLECTIONS__RICH_ITERABLE)) {
            return new EclipseCollectionComponent(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        }
        return null;
    }

}

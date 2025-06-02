package io.github.cbarlin.aru.impl.types.dependencies.fastutils;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.*;

import javax.lang.model.element.RecordComponentElement;

import org.jspecify.annotations.Nullable;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ComponentAnalyser;

@ServiceProvider
public class FastUtilsCollectionAnalyser implements ComponentAnalyser  {

    @Override
    public int specificity() {
        return 2;
    }

    @Override
    public @Nullable AnalysedComponent analyseComponent(RecordComponentElement element, AnalysedRecord parentRecord, boolean isIntendedConstructorParam, UtilsProcessingContext utilsProcessingContext) {
        if (
            OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__BOOLEAN_COLLECTION) ||
            OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__BYTE_COLLECTION) ||
            OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__CHAR_COLLECTION) ||
            OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__DOUBLE_COLLECTION) ||
            OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__FLOAT_COLLECTION) ||
            OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__INT_COLLECTION) ||
            OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__LONG_COLLECTION) ||
            OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__SHORT_COLLECTION)
        ) {
            return new FastUtilsCollectionComponent(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        }
        return null;
    }

}

package io.github.cbarlin.aru.core.types;

import java.util.Optional;

import javax.lang.model.element.RecordComponentElement;

import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.UtilsProcessingContext;

import io.avaje.spi.Service;

/**
 * A service that can create an {@link AnalysedComponent}
 */
@Service
public interface ComponentAnalyser extends Comparable<ComponentAnalyser> {

    /**
     * How specific this is at creating AnalysedComponent
     */
    public int specificity();

    /**
     * Analyse the component, if possible. If not, return null.
     */
    @Nullable
    public AnalysedComponent analyseComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord, 
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    );

    public default Optional<AnalysedComponent> analyse(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord, 
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        return Optional.ofNullable(analyseComponent(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext));
    }

    @Override
    public default int compareTo(ComponentAnalyser o) {
        return Integer.compare(specificity(), o.specificity());
    }
}

package io.github.cbarlin.aru.core.impl.types.analyser;

import javax.lang.model.element.RecordComponentElement;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.wiring.ResetPerComponent;

@Factory
public final class BasicAnalyser {

    @Bean
    @ResetPerComponent
    @BeanTypes(BasicAnalysedComponent.class)
    BasicAnalysedComponent analysedComponent(
        final @External RecordComponentElement recordComponentElement, 
        final AnalysedRecord analysedRecord, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        final boolean isIntendedConstructorParam = analysedRecord.intendedConstructor().getParameters()
            .stream()
            .filter(param -> param.getSimpleName().toString().equals(recordComponentElement.getSimpleName().toString()))
            .filter(param -> APContext.types().isSameType(param.asType(), recordComponentElement.asType()))
            .findFirst()
            .isPresent();
        return new BasicAnalysedComponent(recordComponentElement, analysedRecord, isIntendedConstructorParam, utilsProcessingContext);
    }

}

package io.github.cbarlin.aru.core.impl.types.analyser;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.avaje.inject.Primary;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.wiring.CorePerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.RecordComponentElement;
import java.util.Optional;

@Factory
@CorePerComponentScope
public final class BasicAnalyser {

    @Bean
    @Primary
    @CorePerComponentScope
    @BeanTypes({BasicAnalysedComponent.class, AnalysedComponent.class})
    BasicAnalysedComponent analysedComponent(
        final RecordComponentElement recordComponentElement, 
        final AnalysedRecord analysedRecord, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        final boolean isIntendedConstructorParam = analysedRecord.intendedConstructor().getParameters()
            .stream()
            .filter(param -> param.getSimpleName().toString().equals(recordComponentElement.getSimpleName().toString()))
            .anyMatch(param -> APContext.types().isSameType(param.asType(), recordComponentElement.asType()));
        final Optional<ProcessingTarget> target = findTarget(recordComponentElement, utilsProcessingContext);
        return new BasicAnalysedComponent(recordComponentElement, analysedRecord, isIntendedConstructorParam, utilsProcessingContext, target);
    }

    private static Optional<ProcessingTarget> findTarget(
            final RecordComponentElement recordComponentElement,
            final UtilsProcessingContext utilsProcessingContext
    ) {
        return findTarget(TypeName.get(recordComponentElement.asType()), utilsProcessingContext);
    }

    private static Optional<ProcessingTarget> findTarget(
            final TypeName typeName,
            final UtilsProcessingContext utilsProcessingContext
    ) {
        return switch (typeName) {
            case final ParameterizedTypeName ptn when ptn.typeArguments.size() == 1 -> extractFromClassName(ptn.typeArguments.getFirst(), utilsProcessingContext);
            case final ClassName className -> extractFromClassName(className, utilsProcessingContext);
            default -> Optional.empty();
        };
    }

    private static Optional<ProcessingTarget> extractFromClassName(
            final @Nullable TypeName typeName,
            final UtilsProcessingContext utilsProcessingContext
    ) {
        return Optional.ofNullable(typeName)
                .filter(ClassName.class::isInstance)
                .map(ClassName.class::cast)
                .map(ClassName::canonicalName)
                .map(APContext.elements()::getTypeElement)
                .map(utilsProcessingContext::analysedType);
    }
}

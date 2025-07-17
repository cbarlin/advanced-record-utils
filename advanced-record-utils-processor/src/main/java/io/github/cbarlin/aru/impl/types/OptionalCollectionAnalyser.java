package io.github.cbarlin.aru.impl.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import java.util.List;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.TypeMirror;

import org.jspecify.annotations.Nullable;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ComponentAnalyser;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class OptionalCollectionAnalyser implements ComponentAnalyser {

    @Override
    public int specificity() {
        return 3;
    }

    @Override
    public @Nullable AnalysedComponent analyseComponent(RecordComponentElement element, AnalysedRecord parentRecord, boolean isIntendedConstructorParam, UtilsProcessingContext utilsProcessingContext) {
        final TypeMirror componentType = element.asType();
        final TypeName componentTypeName = TypeName.get(componentType);
        if (componentTypeName instanceof final ParameterizedTypeName outerWrapper && OPTIONAL.equals(outerWrapper.rawType)) {
            // OK, let's start working this out!
            final List<TypeName> outerTargs = outerWrapper.typeArguments;
            if (outerTargs.size() == 1 && outerTargs.get(0) instanceof final ParameterizedTypeName nextWrapper && nextWrapper.typeArguments.size() == 1) {
                final boolean isCollectionType = OptionalClassDetector.checkSameOrSubType(nextWrapper, COLLECTION);
                if (isCollectionType) {
                    return new AnalysedOptionalCollection(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
                }
            }
        }
        return null;
    }

}

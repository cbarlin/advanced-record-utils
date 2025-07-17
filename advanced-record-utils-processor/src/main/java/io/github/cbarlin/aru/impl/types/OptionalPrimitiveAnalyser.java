package io.github.cbarlin.aru.impl.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_DOUBLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_INT;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL_LONG;

import java.util.Set;

import javax.lang.model.element.RecordComponentElement;

import org.jspecify.annotations.Nullable;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ComponentAnalyser;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class OptionalPrimitiveAnalyser implements ComponentAnalyser {

    private static final Set<TypeName> NAMES = Set.of(OPTIONAL_DOUBLE, OPTIONAL_INT, OPTIONAL_LONG);

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
        if (NAMES.contains(TypeName.get(element.asType()))) {
            return new AnalysedOptionalPrimitiveComponent(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        }
        return null;
    }

}

package io.github.cbarlin.aru.impl.types;

import javax.lang.model.element.RecordComponentElement;

import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

public class TypeAliasComponent extends AnalysedComponent {

    private final ClassName aliasFor;

    public TypeAliasComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord,
        final boolean isIntendedConstructorParam,
        final UtilsProcessingContext utilsProcessingContext,
        final ClassName aliasFor
    ) {
        super(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        this.aliasFor = aliasFor;
    }

    @Override
    public TypeName serialisedTypeName() {
        return aliasFor;
    }

    @Override
    public boolean proceedDownTree() {
        return false;
    }

    @Override
    public void setAnalysedType(final ProcessingTarget type) {
        // Do nothing, this shouldn't be used
    }
}

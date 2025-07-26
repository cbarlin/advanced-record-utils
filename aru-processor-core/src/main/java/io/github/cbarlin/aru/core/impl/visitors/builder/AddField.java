package io.github.cbarlin.aru.core.impl.visitors.builder;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.ResetPerRecord;
import io.micronaut.sourcegen.javapoet.FieldSpec;

@Component
@ResetPerRecord
public class AddField extends RecordVisitor {

    public AddField(final AnalysedRecord analysedRecord) {
        super(CommonsConstants.Claims.CORE_BUILDER_FIELD, analysedRecord);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final FieldSpec spec = FieldSpec.builder(analysedComponent.typeName(), analysedComponent.name(), Modifier.PRIVATE)
                .addAnnotation(CommonsConstants.Names.NULLABLE)
                .build();
            analysedRecord.builderArtifact().addField(spec);
            return true;
        }
        return false;
    }
}

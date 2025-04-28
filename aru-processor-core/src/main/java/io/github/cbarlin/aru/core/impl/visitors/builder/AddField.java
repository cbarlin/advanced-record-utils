package io.github.cbarlin.aru.core.impl.visitors.builder;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.FieldSpec;

@ServiceProvider
public class AddField extends RecordVisitor {

    public AddField() {
        super(CommonsConstants.Claims.CORE_BUILDER_FIELD);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final FieldSpec spec = FieldSpec.builder(analysedComponent.typeName(), analysedComponent.name(), Modifier.PRIVATE)
                .addAnnotation(CommonsConstants.Names.NULLABLE)
                .build();
            analysedComponent.builderArtifact().addField(spec);
            return true;
        }
        return false;
    }
}

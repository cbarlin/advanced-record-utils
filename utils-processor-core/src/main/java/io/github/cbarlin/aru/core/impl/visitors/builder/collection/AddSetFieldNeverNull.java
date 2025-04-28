package io.github.cbarlin.aru.core.impl.visitors.builder.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.HASH_SET;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.collection.SetRecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.FieldSpec;

@ServiceProvider
public class AddSetFieldNeverNull extends SetRecordVisitor {

    public AddSetFieldNeverNull() {
        super(CommonsConstants.Claims.CORE_BUILDER_FIELD);
    }

    @Override
    protected int setSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitSetComponent(AnalysedCollectionComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final FieldSpec spec = FieldSpec.builder(analysedComponent.typeName(), analysedComponent.name(), Modifier.PRIVATE)
                .addAnnotation(CommonsConstants.Names.NON_NULL)
                .initializer("new $T<>()", HASH_SET)
                .build();
            analysedComponent.builderArtifact().addField(spec);
            return true;
        }
        return false;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.settings().prism().builderOptions().buildNullCollectionToEmpty());
    }
}

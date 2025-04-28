package io.github.cbarlin.aru.core.impl.visitors.builder.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARRAY_LIST;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.collection.ListRecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.FieldSpec;

@ServiceProvider
public class AddListFieldNeverNull extends ListRecordVisitor {

    public AddListFieldNeverNull() {
        super(CommonsConstants.Claims.CORE_BUILDER_FIELD);
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.settings().prism().builderOptions().buildNullCollectionToEmpty());
    }

    @Override
    protected int listSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitListComponent(AnalysedCollectionComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final FieldSpec spec = FieldSpec.builder(analysedComponent.typeName(), analysedComponent.name(), Modifier.PRIVATE)
                .addAnnotation(CommonsConstants.Names.NON_NULL)
                .initializer("new $T<>()", ARRAY_LIST)
                .build();
            analysedComponent.builderArtifact().addField(spec);
            return true;
        }
        return false;
    }
}

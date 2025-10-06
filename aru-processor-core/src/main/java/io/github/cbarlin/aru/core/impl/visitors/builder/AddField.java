package io.github.cbarlin.aru.core.impl.visitors.builder;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerComponentScope;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@CorePerComponentScope
public final class AddField extends RecordVisitor {

    final BasicAnalysedComponent analysedComponent;

    public AddField(final AnalysedRecord analysedRecord, final BasicAnalysedComponent basicAnalysedComponent) {
        super(CommonsConstants.Claims.CORE_BUILDER_FIELD, analysedRecord);
        this.analysedComponent = basicAnalysedComponent;
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent ignored) {
        if(analysedComponent.isIntendedConstructorParam()) {
            // This was changed as part of #121 when making the delayed version of the setter because
            //   it was discovered that annotating the field doesn't work 100% of the time...
            final TypeName annotatedType = analysedComponent.typeName().annotated(AnnotationSpec.builder(CommonsConstants.Names.NULLABLE).build());
            final FieldSpec spec = FieldSpec.builder(annotatedType, analysedComponent.name(), Modifier.PRIVATE)
                .build();
            analysedRecord.builderArtifact().addField(spec);
            return true;
        }
        return false;
    }
}

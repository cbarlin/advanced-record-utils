package io.github.cbarlin.aru.core.impl.visitors.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@Component
@CorePerRecordScope
public final class AddGetter extends RecordVisitor {

    public AddGetter(final AnalysedRecord analysedRecord) {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER, analysedRecord);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final String name = analysedComponent.name();
            final MethodSpec.Builder method = analysedRecord.builderArtifact()
                .createMethod(name, claimableOperation, analysedComponent.element())
                .returns(analysedComponent.typeName())
                .addAnnotation(NULLABLE)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Returns the current value of {@code $L}\n", name)
                .addStatement("return this.$L", name);
            
            AnnotationSupplier.addGeneratedAnnotation(method, this);
        }
        return false;
    }
}

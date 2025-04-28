package io.github.cbarlin.aru.core.impl.visitors.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class AddGetter extends RecordVisitor {

    public AddGetter() {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER);
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
            final String name = analysedComponent.name();
            final MethodSpec.Builder method = analysedComponent.builderArtifact()
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

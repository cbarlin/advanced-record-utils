package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;

@ServiceProvider
public class AddOptionalPrimitive extends RecordVisitor {

    public AddOptionalPrimitive() {
        super(Claims.BUILDER_CONCRETE_OPTIONAL);
    }

    @Override
    public int specificity() {
        return 1;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord target) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if(analysedComponent instanceof final AnalysedOptionalPrimitiveComponent component && analysedComponent.isIntendedConstructorParam()) {
            final ClassName builderClassName = component.builderArtifact().className();
            final String name = component.name();
            final ParameterSpec param = ParameterSpec.builder(component.unNestedPrimaryTypeName(), name, Modifier.FINAL)
                .addJavadoc("The replacement value")
                // No need for nullness annotation - this is a primitive!
                .build();
            final MethodSpec.Builder method = component.builderArtifact().createMethod(name, claimableOperation, analysedComponent)
                .addJavadoc("Updates the value of {@code $L}", name)
                .returns(builderClassName)
                .addParameter(param)
                .addAnnotation(NOT_NULL)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return this.$L($T.of($L))", name, component.typeName(), name);
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }    
}

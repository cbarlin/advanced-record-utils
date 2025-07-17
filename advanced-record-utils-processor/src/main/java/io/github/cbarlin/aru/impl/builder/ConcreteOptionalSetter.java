package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.impl.types.OptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.OptionalRecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public final class ConcreteOptionalSetter extends OptionalRecordVisitor {

    public ConcreteOptionalSetter() {
        super(Claims.BUILDER_CONCRETE_OPTIONAL);
    }

    @Override
    protected int optionalSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitOptionalComponent(OptionalComponent<?> analysedComponent) {
        if (analysedComponent.isIntendedConstructorParam() && analysedComponent instanceof AnalysedOptionalComponent) {
            final ClassName builderClassName = analysedComponent.builderArtifact().className();
            final String name = analysedComponent.name();
            final ParameterSpec param = ParameterSpec.builder(analysedComponent.unNestedPrimaryTypeName(), name, Modifier.FINAL)
                .addJavadoc("The replacement value")
                .addAnnotation(NULLABLE)
                .build();
            
            final MethodSpec.Builder method = analysedComponent.builderArtifact().createMethod(name, claimableOperation, analysedComponent.component())
                .addJavadoc("Updates the value of {@code $L}", name)
                .returns(builderClassName)
                .addParameter(param)
                .addAnnotation(NOT_NULL)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return this.$L($T.ofNullable($L))", name, OPTIONAL, name);
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.prism().builderOptions().concreteSettersForOptional());
    }

}

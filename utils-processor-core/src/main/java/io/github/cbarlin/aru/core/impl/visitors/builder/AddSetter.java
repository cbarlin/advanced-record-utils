package io.github.cbarlin.aru.core.impl.visitors.builder;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class AddSetter extends RecordVisitor {

    private ClassName builderClassName;

    public AddSetter() {
        super(CommonsConstants.Claims.CORE_BUILDER_SETTER);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        builderClassName = analysedRecord.builderArtifact().className();
        return true;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final String name = analysedComponent.name();
            final ParameterSpec param = ParameterSpec.builder(analysedComponent.typeName(), name, Modifier.FINAL)
                .addJavadoc("The replacement value")
                .build();
            
            final var method = analysedComponent.builderArtifact().createMethod(analysedComponent.name(), claimableOperation, analysedComponent)
                .addJavadoc("Updates the value of {@code $L}", name)
                .returns(builderClassName)
                .addParameter(param)
                .addAnnotation(CommonsConstants.Names.NON_NULL)
                .addModifiers(Modifier.PUBLIC);

            if (!Boolean.FALSE.equals(analysedComponent.settings().prism().builderOptions().nullReplacesNotNull())) {
                method.addStatement("this.$L = $L", name, name)
                    .addJavadoc("\n<p>\n")
                    .addJavadoc("Supplying a null value will set the current value to null");
            } else {
                method.addStatement("this.$L = $T.nonNull($L) ? $L : this.$L", name, CommonsConstants.Names.OBJECTS, name, name, name)
                    .addJavadoc("\n<p>\n")
                    .addJavadoc("Supplying a null value won't replace a set value");
            }

            method.addStatement("return this");

            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }
}

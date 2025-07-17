package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public final class WithMethodOnField extends WitherVisitor {

    public WithMethodOnField() {
        super(Claims.WITHER_WITH);
    }

    @Override
    protected boolean isWitherApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int witherSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (analysedComponent.isIntendedConstructorParam()) {
            final String name = analysedComponent.name();
            final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(name) + witherOptionsPrism.withMethodSuffix();
            final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, analysedComponent)
                .addAnnotation(NON_NULL)
                .returns(analysedComponent.parentRecord().intendedType())
                .addModifiers(Modifier.DEFAULT)
                .addJavadoc("Return a new instance with a different {@code $L} field", name)
                .addParameter(
                    ParameterSpec.builder(analysedComponent.typeName(), name, Modifier.FINAL)
                        .addJavadoc("Replacement value")
                        .build()
                )
                .addStatement("return this.$L()\n.$L($L)\n.$L()", witherOptionsPrism.convertToBuilder(), name, name, builderOptionsPrism.buildMethodName());
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            return true;
        }
        return false;
    }
}

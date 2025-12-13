package io.github.cbarlin.aru.impl.wither;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.WitherPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;

@Singleton
@WitherPerComponentScope
@RequiresBean({ConstructorComponent.class})
public final class WithMethodOnField extends WitherVisitor {

    public WithMethodOnField(final WitherInterface witherInterface, final AnalysedRecord analysedRecord) {
        super(Claims.WITHER_WITH, witherInterface, analysedRecord);
    }

    @Override
    protected int witherSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String name = analysedComponent.name();
        final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(name) + witherOptionsPrism.withMethodSuffix();
        final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, analysedComponent)
            .returns(analysedComponent.parentRecord().intendedType())
            .addModifiers(Modifier.DEFAULT)
            .addJavadoc("Return a new instance with a different {@code $L} field", name)
            .addParameter(
                ParameterSpec.builder(analysedComponent.typeNameNullable(), name, Modifier.FINAL)
                    .addJavadoc("Replacement value")
                    .build()
            )
            .addStatement("return this.$L()\n.$L($L)\n.$L()", witherOptionsPrism.convertToBuilder(), name, name, builderOptionsPrism.buildMethodName());
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}

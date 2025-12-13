package io.github.cbarlin.aru.impl.wither;

import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.WitherPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;

@Singleton
@WitherPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresProperty(value = "createAdderMethods", equalTo = "true")
@RequiresBean({ConstructorComponent.class, ComponentTargetingRecord.class, AnalysedCollectionComponent.class})
public final class WithFluentMethodAdder extends WitherVisitor {

    private final ProcessingTarget other;

    public WithFluentMethodAdder(
            final WitherInterface witherInterface,
            final AnalysedRecord analysedRecord,
            final ComponentTargetingRecord componentTargetingRecord
    ) {
        super(Claims.WITHER_WITH_FLUENT, witherInterface, analysedRecord);
        this.other = componentTargetingRecord.target();
    }

    @Override
    protected int witherSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String name = analysedComponent.name();
        final ClassName otherBuilderClassName = other.builderArtifact().className();

        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(CONSUMER, otherBuilderClassName);
        final ParameterSpec paramSpec = ParameterSpec.builder(paramTypeName, "subBuilder", Modifier.FINAL)
                .addJavadoc("Builder that can be used to replace {@code $L}", name)
                .build();

        final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(builderOptionsPrism.adderMethodPrefix()) + capitalise(name) + builderOptionsPrism.adderMethodSuffix() + witherOptionsPrism.withMethodSuffix();
        final String builderMethodName = builderOptionsPrism.adderMethodPrefix() + capitalise(name) + builderOptionsPrism.adderMethodSuffix();
        final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, analysedComponent)
            .returns(analysedComponent.parentRecord().intendedType())
            .addModifiers(Modifier.DEFAULT)
            .addJavadoc("Return a new instance with a different {@code $L} field, obtaining the value by invoking the builder", name)
            .addParameter(paramSpec)
            .addStatement("return this.$L()\n.$L(subBuilder)\n.$L()", witherOptionsPrism.convertToBuilder(), builderMethodName, builderOptionsPrism.buildMethodName());
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        analysedComponent.parentRecord().addCrossReference(other);
        return true;  
    }
    
}

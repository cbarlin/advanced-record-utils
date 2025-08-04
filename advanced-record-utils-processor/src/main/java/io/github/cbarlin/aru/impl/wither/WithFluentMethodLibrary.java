package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingLibraryLoaded;
import io.github.cbarlin.aru.impl.wiring.WitherPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import jakarta.inject.Singleton;

@Singleton
@WitherPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresBean({ConstructorComponent.class, ComponentTargetingLibraryLoaded.class})
public final class WithFluentMethodLibrary extends WitherVisitor {

    private final ProcessingTarget other;
    public WithFluentMethodLibrary(final WitherInterface witherInterface, final AnalysedRecord analysedRecord, final ComponentTargetingLibraryLoaded tar) {
        super(Claims.WITHER_WITH_FLUENT, witherInterface, analysedRecord);
        this.other = tar.target();
    }

    @Override
    protected int witherSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final String name = analysedComponent.name();
        final ClassName otherBuilderClassName = other.builderArtifact().className();

        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(CONSUMER, otherBuilderClassName);
        final ParameterSpec paramSpec = ParameterSpec.builder(paramTypeName, "subBuilder", Modifier.FINAL)
                .addAnnotation(NON_NULL)
                .addJavadoc("Builder that can be used to replace {@code $L}", name)
                .build();

        final String withMethodName = witherOptionsPrism.withMethodPrefix() + capitalise(name) + witherOptionsPrism.withMethodSuffix();
        final MethodSpec.Builder methodBuilder = witherInterface.createMethod(withMethodName, claimableOperation, analysedComponent)
            .addAnnotation(NON_NULL)
            .returns(analysedComponent.parentRecord().intendedType())
            .addModifiers(Modifier.DEFAULT)
            .addJavadoc("Return a new instance with a different {@code $L} field, obtaining the value by invoking the builder", name)
            .addParameter(paramSpec)
            .addStatement("return this.$L()\n.$L(subBuilder)\n.$L()", witherOptionsPrism.convertToBuilder(), name, builderOptionsPrism.buildMethodName());
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        analysedComponent.parentRecord().addCrossReference(other);
        return true;  
    }
    
}

package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.types.ComponentTargetingInterface;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresBean({ConstructorComponent.class, ComponentTargetingInterface.class})
public final class AddFluentSetterFromInterface extends RecordVisitor {

    private final ComponentTargetingInterface cti;
    private final BuilderClass builder;

    public AddFluentSetterFromInterface(final ComponentTargetingInterface cti, final BuilderClass builderClass) {
        super(Claims.BUILDER_FLUENT_SETTER, cti.parentRecord());
        this.cti = cti;
        this.builder = builderClass;
    }

    @Override
    public int specificity() {
        return 3;
    }
    
    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {

        final AnalysedInterface other = cti.target();
        final String multiTypeSetterBridge = settings.prism().builderOptions().multiTypeSetterBridge();
        final String name = analysedComponent.name();

        for (final ProcessingTarget asTarget : other.implementingTypes()) {
            if (!concreteImplementingType(asTarget)) {
                continue;
            }
            
            final String emptyMethodName = asTarget.prism().builderOptions().emptyCreationName();
            final String copyMethodName = asTarget.prism().builderOptions().copyCreationName();
            final String buildMethodName = asTarget.prism().builderOptions().buildMethodName();
            final ClassName otherBuilderClassName = asTarget.builderArtifact().className();
            final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(CONSUMER, otherBuilderClassName);
            final ParameterSpec paramSpec = ParameterSpec.builder(paramTypeName, "subBuilder", Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .addJavadoc("Builder that can be used to replace {@code $L}", name)
                    .build();

            final ClassName targetCN = ClassName.get(asTarget.typeElement());
            final String methodName = name + multiTypeSetterBridge + targetCN.simpleName();

            final MethodSpec.Builder methodBuilder = builder.createMethod(methodName, claimableOperation, asTarget.typeElement(), paramTypeName)
                .addAnnotation(NON_NULL)
                .returns(builder.className())
                .addParameter(paramSpec)
                .addJavadoc("Uses a supplied builder to build an instance of {@link $T} and replace the value of {@link $L}", targetCN, name)
                .addStatement("$T.requireNonNull(subBuilder, $S)", OBJECTS, "Cannot supply a null function argument")
                .addStatement("final $T builder", otherBuilderClassName)
                .beginControlFlow("if ($T.nonNull(this.$L()) && this.$L() instanceof $T oth)", OBJECTS, name, name, targetCN)
                .addStatement("builder = $T.$L(oth)", otherBuilderClassName, copyMethodName)
                .nextControlFlow("else")
                .addStatement("builder = $T.$L()", otherBuilderClassName, emptyMethodName)
                .endControlFlow();

            logTrace(methodBuilder, "Passing over to provided consumer");
                
            methodBuilder.addStatement("subBuilder.accept(builder)")
                .addStatement("return this.$L(builder.$L())", name, buildMethodName);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        }

        return true;
    }

    private boolean concreteImplementingType (final ProcessingTarget processingTarget) {
        return (processingTarget instanceof AnalysedRecord) || 
            (processingTarget instanceof LibraryLoadedTarget);
    }

}

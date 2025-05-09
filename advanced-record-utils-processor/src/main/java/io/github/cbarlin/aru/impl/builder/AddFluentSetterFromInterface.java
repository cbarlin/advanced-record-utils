package io.github.cbarlin.aru.impl.builder;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.BuilderOptionsPrism;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public class AddFluentSetterFromInterface extends RecordVisitor {

    public AddFluentSetterFromInterface() {
        super(Claims.BUILDER_FLUENT_SETTER);
    }

    @Override
    public int specificity() {
        return 3;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.settings().prism().builderOptions().fluent());
    }
    
    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (supportedComponent(analysedComponent)) {
            // The method call handles these
            @SuppressWarnings({"java:S1854", "java:S3655"})
            final AnalysedInterface other = (AnalysedInterface) analysedComponent.targetAnalysedType().get();

            final ToBeBuilt builder = analysedComponent.builderArtifact();
            final BuilderOptionsPrism prism = analysedComponent.settings().prism().builderOptions();
            final String name = analysedComponent.name();

            other.implementingTypes()
                .stream()
                .filter(this::concreteImplementingType)
                .forEach(target -> {
                    createAdder(target, builder, prism, name);
                    analysedComponent.addCrossReference(target);
                });

            return true;
        }
        return false;
    }

    private void createAdder(final ProcessingTarget asTarget, final ToBeBuilt builder, final BuilderOptionsPrism prims, final String name) {
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
        final String methodName = name + prims.multiTypeSetterBridge() + targetCN.simpleName();

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

    private boolean concreteImplementingType (final ProcessingTarget processingTarget) {
        return (processingTarget instanceof AnalysedRecord) || 
            (processingTarget instanceof LibraryLoadedTarget);
    }

    // Somehow we cannot see the "isPresent" check on the previous line...
    @SuppressWarnings({"java:S3655"})
    private boolean supportedComponent(final AnalysedComponent analysedComponent) {
        return analysedComponent.targetAnalysedType().isPresent() && 
            (analysedComponent.targetAnalysedType().get() instanceof AnalysedInterface);
    }
}

package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.impl.Constants.Claims.BUILDER_FLUENT_SETTER;
import static io.github.cbarlin.aru.impl.Constants.Claims.WITHER_WITH_FLUENT;
import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public final class WithFluentMethod extends WitherVisitor {

    public WithFluentMethod() {
        super(WITHER_WITH_FLUENT);
    }

    @Override
    protected int witherSpecificity() {
        return 1;
    }

    @Override
    protected boolean isWitherApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (analysedComponent.isClaimed(BUILDER_FLUENT_SETTER) && supportedComponent(analysedComponent)) {
            // The builder handled these when called
            @SuppressWarnings({"java:S1854", "java:S3655"})
            final ProcessingTarget other = analysedComponent.targetAnalysedType().get();
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
        return false;
    }

    // Somehow we cannot see the "isPresent" check on the previous line...
    @SuppressWarnings({"java:S3655"})
    private boolean supportedComponent(final AnalysedComponent analysedComponent) {
        return 
            (!analysedComponent.requiresUnwrapping()) &&
            analysedComponent.targetAnalysedType().isPresent() && 
            (
                (analysedComponent.targetAnalysedType().get() instanceof AnalysedRecord) ||
                (analysedComponent.targetAnalysedType().get() instanceof LibraryLoadedTarget)
            );
    }
    
}

package io.github.cbarlin.aru.impl.builder.collection;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public final class AddFluentAdderNonInterface extends CollectionRecordVisitor {

    public AddFluentAdderNonInterface() {
        super(Claims.BUILDER_FLUENT_SETTER);
    }

    @Override
    protected int collectionSpecificity() {
        return 3;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.settings().prism().builderOptions().fluent());
    }

    @Override
    protected boolean visitCollectionComponent(AnalysedCollectionComponent analysedComponent) {
        if (supportedComponent(analysedComponent)) {
            // The method call handles these
            @SuppressWarnings({"java:S1854", "java:S3655"})
            final ProcessingTarget other = analysedComponent.targetAnalysedType().get();

            final String name = analysedComponent.name();

            final String emptyMethodName = other.prism().builderOptions().emptyCreationName();
            final String buildMethodName = other.prism().builderOptions().buildMethodName();

            final ClassName otherBuilderClassName = other.builderArtifact().className();
            final ToBeBuilt myBuilder = analysedComponent.builderArtifact();
            final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(CONSUMER, otherBuilderClassName);
            final ParameterSpec paramSpec = ParameterSpec.builder(paramTypeName, "subBuilder", Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .addJavadoc("Builder that can be used to replace {@code $L}", name)
                    .build();
            final var methodBuilder = myBuilder.createMethod(addNameMethodName(analysedComponent), claimableOperation, analysedComponent, CONSUMER)
                .addAnnotation(NON_NULL)
                .returns(myBuilder.className())
                .addParameter(paramSpec)
                .addJavadoc("Uses a supplied builder to replace the value at {@code $L}", name)
                .addStatement("$T.requireNonNull(subBuilder, $S)", OBJECTS, "Cannot supply a null function argument")
                .addStatement("final $T builder = $T.$L()", otherBuilderClassName, otherBuilderClassName, emptyMethodName);
            
            logTrace(methodBuilder, "Passing over to provided consumer");
            
            methodBuilder.addStatement("subBuilder.accept(builder)")
                .addStatement("return this.$L(builder.$L())", addNameMethodName(analysedComponent), buildMethodName);
            AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
            analysedComponent.addCrossReference(other);
            return true;
        }
        return false;
    }

    // Somehow we cannot see the "isPresent" check on the previous line...
    @SuppressWarnings({"java:S3655"})
    private boolean supportedComponent(final AnalysedCollectionComponent analysedComponent) {
        return analysedComponent.targetAnalysedType().isPresent() && 
            ((analysedComponent.targetAnalysedType().get() instanceof AnalysedRecord) ||
            (analysedComponent.targetAnalysedType().get() instanceof LibraryLoadedTarget));
    }

}

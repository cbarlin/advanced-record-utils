package io.github.cbarlin.aru.impl.builder.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.ITERABLE;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public final class AddAddAllIterable extends CollectionRecordVisitor {

    public AddAddAllIterable() {
        super(Claims.BUILDER_ADD_ALL_ITERABLE);
    }

    @Override
    protected int collectionSpecificity() {
        return 1;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected boolean visitCollectionComponent(AnalysedCollectionComponent analysedCollectionComponent) {
        if (analysedCollectionComponent.isIntendedConstructorParam()) {
            final String name = analysedCollectionComponent.name();
            final ParameterizedTypeName iterableParamType = ParameterizedTypeName.get(ITERABLE, analysedCollectionComponent.unNestedPrimaryTypeName());
            final ParameterSpec param = ParameterSpec.builder(iterableParamType, name, Modifier.FINAL)
                .addJavadoc("A collection to be merged into the collection")
                .addAnnotation(NOT_NULL)
                .build();
            final String addName = addNameMethodName(analysedCollectionComponent);
            final MethodSpec.Builder method = analysedCollectionComponent.builderArtifact()
                .createMethod(addName, claimableOperation, ITERABLE)
                .addJavadoc("Adds all elements of the provided iterable to {@code $L}", name)
                .addParameter(param)
                .returns(analysedCollectionComponent.builderArtifact().className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, name)
                .beginControlFlow("for (final $T __addable : $L)", analysedCollectionComponent.unNestedPrimaryTypeName(), name)
                .addStatement("this.$L(__addable)", addName)
                .endControlFlow()
                .endControlFlow()
                .addStatement("return this");

            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }
}

package io.github.cbarlin.aru.core.impl.visitors.builder.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.HASH_SET;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.collection.SetRecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class AddSetSingleAdder extends SetRecordVisitor {

    private ClassName builderClassName;

    public AddSetSingleAdder() {
        super(CommonsConstants.Claims.CORE_BUILDER_SINGLE_ITEM_ADDER);
    }

    @Override
    protected boolean visitSetComponent(AnalysedCollectionComponent acc) {
        if (acc.isIntendedConstructorParam()) {
            final String name = acc.name();
            final var singularType = acc.unNestedPrimaryTypeName();
            final String methodName = addNameMethodName(acc);
            final ParameterSpec param = ParameterSpec.builder(singularType, name, Modifier.FINAL)
                .addJavadoc("A singular instance to be added to the collection")
                .addAnnotation(NULLABLE)
                .build();

            
            final var method = acc.builderArtifact().createMethod(methodName, claimableOperation)
                .returns(builderClassName)
                .addAnnotation(NOT_NULL)
                .addParameter(param)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Add a singular {@link $T} to the collection for the field {@code $L}", singularType, name)
                // Check that we aren't trying to add to null
                .beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, name)
                    .addStatement("this.$L(new $T<>())", name, HASH_SET)
                .endControlFlow()
                // Add a try in case someone set the collection to be immutable...
                .beginControlFlow("try")
                    .addStatement("this.$L.add($L)", name, name)
                .nextControlFlow("catch ($T ex)", UNSUPPORTED_OPERATION_EXCEPTION);
                logDebug(method, "UnsupportedOperationException when attempting to add to collection - recreating collection");
                method.addStatement("this.$L(new $T<>(this.$L))", name, HASH_SET, name)
                    .addStatement("this.$L.add($L)", name, name)
                .endControlFlow()
                .addStatement("return this");

            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }

    @Override
    public int setSpecificity() {
        return 0;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        builderClassName = analysedRecord.builderArtifact().className();
        return true;
    }
}

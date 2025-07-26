package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.WitherPerRecordScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import jakarta.inject.Singleton;

@Singleton
@WitherPerRecordScope
@RequiresProperty(value = "fluent", equalTo = "true")
public final class BuilderFluent extends WitherVisitor {

    public BuilderFluent(final WitherInterface witherInterface, final AnalysedRecord analysedRecord) {
        super(Claims.WITHER_FLUENT_BUILDER, witherInterface, analysedRecord);
    }

    @Override
    protected int witherSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final ClassName builderClassName = analysedRecord.builderArtifact().className();
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(CONSUMER, builderClassName);
        final ParameterSpec paramSpec = ParameterSpec.builder(paramTypeName, "subBuilder", Modifier.FINAL)
                .addAnnotation(NON_NULL)
                .addJavadoc("A function to modify a new copy of the object")
                .build();

        final MethodSpec.Builder methodBuilder = witherInterface.createMethod(witherOptionsPrism.convertToBuilder(), claimableOperation)
            .addParameter(paramSpec)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Allows creation of a copy of this instance with some tweaks via a builder")
            .addAnnotation(NON_NULL)
            .addModifiers(Modifier.DEFAULT)
            .addStatement("$T.requireNonNull(subBuilder, $S)", OBJECTS, "Cannot supply a null function argument")
            .addStatement("final $T ___builder = this.$L()", builderClassName, witherOptionsPrism.convertToBuilder())
            .addStatement("subBuilder.accept(___builder)")
            .addStatement("return ___builder.$L()", builderOptionsPrism.buildMethodName());
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}

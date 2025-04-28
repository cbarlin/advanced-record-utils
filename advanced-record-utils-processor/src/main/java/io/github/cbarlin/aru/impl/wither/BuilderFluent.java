package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.impl.Constants.Claims.WITHER_FLUENT_BUILDER;
import static io.github.cbarlin.aru.impl.Constants.Names.CONSUMER;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.Constants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

public class BuilderFluent extends WitherVisitor {

    public BuilderFluent(){
        super(WITHER_FLUENT_BUILDER);
    }

    @Override
    protected boolean isWitherApplicable(AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(builderOptionsPrism.fluent());
    }

    @Override
    protected int witherSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitStartOfClassImpl(AnalysedRecord analysedRecord) {
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
            .addStatement("return subBuilder.apply(this.$L()).$L()", witherOptionsPrism.witherName(), builderOptionsPrism.buildMethodName());
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        return true;
    }
}

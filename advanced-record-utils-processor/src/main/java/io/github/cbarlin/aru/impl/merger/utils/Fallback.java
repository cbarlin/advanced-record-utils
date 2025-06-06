package io.github.cbarlin.aru.impl.merger.utils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class Fallback extends MergerVisitor {

    public Fallback() {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final var targetTn = analysedComponent.typeName();
        final ParameterSpec paramA = ParameterSpec.builder(targetTn, "elA", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The preferred input")
            .build();
        final ParameterSpec paramB = ParameterSpec.builder(targetTn, "elB", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The non-preferred input")
            .build();
        
        final MethodSpec.Builder method = mergerStaticClass.createMethod(analysedComponent.name(), claimableOperation);
        method.modifiers.clear();
        method.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addParameter(paramA)
            .addParameter(paramB)
            .returns(targetTn)
            .addJavadoc("Merger for the field {@code $L}", analysedComponent.name())
            .beginControlFlow("if ($T.isNull(elA))", OBJECTS)
            .addStatement("return elB")
            .endControlFlow()
            .addStatement("return elA");

        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
    }

}

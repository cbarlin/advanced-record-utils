package io.github.cbarlin.aru.impl.merger.utils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.MERGER_UTILS_CLASS;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class OtherProcessed extends MergerVisitor {

    public OtherProcessed() {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD);
    }

    @Override
    protected boolean innerIsApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (supportedComponent(analysedComponent)) {
            // OK, the other side has a merger utils I can hook into!
            
            // The if statement handled these for us
            @SuppressWarnings({"java:S1854", "java:S3655"})
            final AnalysedRecord other = (AnalysedRecord) analysedComponent.targetAnalysedType().get();

            final ClassName otherMergerClassName = other.utilsClassChildClass(MERGER_UTILS_CLASS, Claims.MERGER_STATIC_CLASS).className();
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
                .addStatement("return $T.merge(elA, elB)", otherMergerClassName);

            AnnotationSupplier.addGeneratedAnnotation(method, this);
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
            (analysedComponent.targetAnalysedType().get() instanceof AnalysedRecord ar) &&
            Boolean.TRUE.equals(ar.settings().prism().merger());
    }
}

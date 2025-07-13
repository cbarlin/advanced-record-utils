package io.github.cbarlin.aru.impl.diff.utils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class NestedDiffCreation extends DifferVisitor {

    private final Set<String> processedSpecs = HashSet.newHashSet(20);

    public NestedDiffCreation() {
        super(Claims.DIFFER_UTILS_COMPUTE_CHANGE);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final Optional<ProcessingTarget> targetAnalysedType = analysedComponent.targetAnalysedType();
        final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
        if (targetAnalysedType.isPresent()) {
            final ProcessingTarget target = targetAnalysedType.get();
            if (target instanceof final AnalysedRecord otherRecord && Boolean.TRUE.equals(otherRecord.settings().prism().diffable()) && (!analysedComponent.requiresUnwrapping()) ) {
                if (processedSpecs.add(methodName)) {
                    createMethod(analysedComponent, methodName, otherRecord);
                }
                return true;
            }
        }
        return false;
    }

    private void createMethod(final AnalysedComponent analysedComponent, final String methodName, final AnalysedRecord otherRecord) {
        // Awesomes! The other side is diffable and hasn't yet been added to the static processor!
        final ClassName otherResultClass = obtainResultClass(otherRecord).className();
        final MethodSpec.Builder builder = differStaticClass.createMethod(methodName, claimableOperation);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.addModifiers(Modifier.FINAL, Modifier.STATIC)
            .returns(otherResultClass)
            .addStatement("return new $T(original, updated)", otherResultClass)
            .addParameter(
                ParameterSpec.builder(analysedComponent.typeName(), "original", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .build()   
            )
            .addParameter(
                ParameterSpec.builder(analysedComponent.typeName(), "updated", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .build()   
            );
    }

}

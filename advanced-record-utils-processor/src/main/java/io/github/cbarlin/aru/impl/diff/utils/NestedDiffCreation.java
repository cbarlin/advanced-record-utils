package io.github.cbarlin.aru.impl.diff.utils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.DiffPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

@Singleton
@DiffPerComponentScope
@RequiresBean({ComponentTargetingRecord.class})
public final class NestedDiffCreation extends DifferVisitor {

    private final Set<String> processedSpecs;
    private final AnalysedRecord target;

    public NestedDiffCreation(final DiffHolder diffHolder, final ComponentTargetingRecord componentTargetingRecord) {
        super(Claims.DIFFER_UTILS_COMPUTE_CHANGE, diffHolder);
        this.processedSpecs = diffHolder.staticClass().createdMethods();
        this.target = componentTargetingRecord.target();
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (!Boolean.TRUE.equals(target.settings().prism().diffable())) {
            return false;
        }
        final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
        if (processedSpecs.add(methodName)) {
            createMethod(analysedComponent, methodName, target);
        }
        return true;
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

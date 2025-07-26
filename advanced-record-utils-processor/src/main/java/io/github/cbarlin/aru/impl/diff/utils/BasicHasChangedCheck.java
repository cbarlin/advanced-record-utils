package io.github.cbarlin.aru.impl.diff.utils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.wiring.DiffPerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

@Singleton
@DiffPerRecordScope
public final class BasicHasChangedCheck extends DifferVisitor {

    public BasicHasChangedCheck(final DiffHolder diffHolder) {
        super(Claims.DIFFER_UTILS_COMPUTE_CHANGE, diffHolder);
    }

    private final Set<String> processedSpecs = HashSet.newHashSet(20);

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
        if (processedSpecs.add(methodName)) {
            // OK, I need to create the method for this particular type name
            final MethodSpec.Builder builder = differStaticClass.createMethod(methodName, claimableOperation);
            AnnotationSupplier.addGeneratedAnnotation(builder, this);
            builder.addModifiers(Modifier.FINAL, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addStatement("return !$T.equals(original, updated)", OBJECTS)
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
        return true;
    }
    
}

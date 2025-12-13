package io.github.cbarlin.aru.impl.merger.utils;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerHolder;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.wiring.MergerPerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;
import java.util.Set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Singleton
@MergerPerRecordScope
public final class Fallback extends MergerVisitor {

    private final Set<String> processedSpecs;

    public Fallback(final MergerHolder mergerHolder) {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD, mergerHolder);
        this.processedSpecs = mergerHolder.processedMethods();
    }
    
    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final var targetTn = analysedComponent.typeNameNullable();
        final String methodName = mergeStaticMethodName(targetTn);
        if (processedSpecs.add(methodName)) {
            final ParameterSpec paramA = ParameterSpec.builder(targetTn, "elA", Modifier.FINAL)
                .addJavadoc("The preferred input")
                .build();
            final ParameterSpec paramB = ParameterSpec.builder(targetTn, "elB", Modifier.FINAL)
                .addJavadoc("The non-preferred input")
                .build();
            
            final MethodSpec.Builder method = mergerStaticClass.createMethod(methodName, claimableOperation);
            method.modifiers.clear();
            method.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addParameter(paramA)
                .addParameter(paramB)
                .returns(targetTn)
                .addJavadoc("Merger for fields of class {@link $T}", targetTn)
                .beginControlFlow("if ($T.isNull(elA))", OBJECTS)
                .addStatement("return elB")
                .endControlFlow()
                .addStatement("return elA");

            AnnotationSupplier.addGeneratedAnnotation(method, this);
        }
        
        return true;
    }

}

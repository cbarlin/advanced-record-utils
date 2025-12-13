package io.github.cbarlin.aru.impl.merger.utils;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerHolder;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalPrimitiveComponent;
import io.github.cbarlin.aru.impl.wiring.MergerPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;
import java.util.Set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Singleton
@MergerPerComponentScope
@RequiresBean({AnalysedOptionalPrimitiveComponent.class})
public final class MergeOptionalPrimitives extends MergerVisitor {

    private final Set<String> processedSpecs;
    private final AnalysedOptionalPrimitiveComponent component;

    public MergeOptionalPrimitives(final MergerHolder mergerHolder, final AnalysedOptionalPrimitiveComponent component) {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD, mergerHolder);
        this.processedSpecs = mergerHolder.processedMethods();
        this.component = component;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final TypeName targetTn = component.typeNameNullable();
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
                .addAnnotation(NOT_NULL)
                .addParameter(paramA)
                .addParameter(paramB)
                .returns(targetTn)
                .addJavadoc("Merger for fields of class {@link $T}", targetTn)
                .beginControlFlow("if ($T.nonNull(elA) && elA.isPresent())", OBJECTS)
                .addStatement("return elA")
                .nextControlFlow("else if($T.nonNull(elB) && elB.isPresent())", OBJECTS)
                .addStatement("return elB")
                .endControlFlow()
                .addStatement("return $T.empty()", targetTn.withoutAnnotations());

            AnnotationSupplier.addGeneratedAnnotation(method, this);
        }
        
        return true;
    }
    
}

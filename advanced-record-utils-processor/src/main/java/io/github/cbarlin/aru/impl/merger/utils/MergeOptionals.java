package io.github.cbarlin.aru.impl.merger.utils;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerHolder;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.wiring.MergerPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;
import java.util.Set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

@Singleton
@MergerPerComponentScope
@RequiresBean({AnalysedOptionalComponent.class})
public final class MergeOptionals extends MergerVisitor {

    private final Set<String> processedSpecs;
    private final AnalysedOptionalComponent aoc;

    public MergeOptionals(final MergerHolder mergerHolder, final AnalysedOptionalComponent aoc) {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD, mergerHolder);
        this.processedSpecs = mergerHolder.processedMethods();
        this.aoc = aoc;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String methodName = mergeStaticMethodName(analysedComponent.typeName());
        if (processedSpecs.add(methodName)) {
            final ParameterSpec paramA = ParameterSpec.builder(analysedComponent.typeNameNullable(), "elA", Modifier.FINAL)
                .addJavadoc("The preferred input")
                .build();
            final ParameterSpec paramB = ParameterSpec.builder(analysedComponent.typeNameNullable(), "elB", Modifier.FINAL)
                .addJavadoc("The non-preferred input")
                .build();
            final var innerTn = aoc.unNestedPrimaryTypeName();
            final MethodSpec.Builder method = mergerStaticClass.createMethod(methodName, claimableOperation);
            method.modifiers.clear();
            method.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addParameter(paramA)
                .addParameter(paramB)
                .returns(analysedComponent.typeNameNonNull())
                .addJavadoc("Merger for fields of class {@link $T}", analysedComponent.typeName())
                .addStatement(
                    """
                return $T.requireNonNullElse(elA, $T.<$T>empty())
.or(() -> $T.requireNonNullElse(elB, $T.<$T>empty()))""",
                    OBJECTS,
                    OPTIONAL,
                    innerTn,
                    OBJECTS,
                    OPTIONAL,
                    innerTn
                );

            AnnotationSupplier.addGeneratedAnnotation(method, this);
        }
        
        return true;
    }
    
}

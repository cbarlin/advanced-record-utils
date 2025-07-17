package io.github.cbarlin.aru.impl.diff.utils;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARRAY_LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTORS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.HASH_SET;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.FUNCTION;
import static io.github.cbarlin.aru.impl.Constants.Names.LIST;
import static io.github.cbarlin.aru.impl.Constants.Names.LONG;
import static io.github.cbarlin.aru.impl.Constants.Names.MAP;
import static io.github.cbarlin.aru.impl.Constants.Names.MATH;
import static io.github.cbarlin.aru.impl.Constants.Names.SET;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.DifferVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public final class ListDiffCreation extends DifferVisitor {
    
    public ListDiffCreation() {
        super(Claims.DIFFER_UTILS_COMPUTE_CHANGE);
    }

    private final Set<String> processedSpecs = HashSet.newHashSet(20);

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if ((!(analysedComponent instanceof final AnalysedCollectionComponent acc)) || (!acc.isList())) {
            return false;
        }
        final String methodName = hasChangedStaticMethodName(analysedComponent.typeName());
        if (!processedSpecs.add(methodName)) {
            return true;
        }
        final MethodSpec.Builder builder = differStaticClass.createMethod(methodName, claimableOperation);
        methodParamsReturn(analysedComponent, acc, builder);

        final ParameterizedTypeName mapPtn = ParameterizedTypeName.get(MAP, acc.unNestedPrimaryTypeName(), LONG);
        builder.addComment("Create frequency maps to count occurrences")
            .addStatement(
                """
                final $T originalFreq = $T.requireNonNullElse(original, $T.<$T>of()).stream()
                    .collect($T.groupingBy($T.identity(), $T.counting()))
                """.trim(),
                mapPtn,
                OBJECTS,
                LIST,
                acc.unNestedPrimaryTypeName(),
                COLLECTORS,
                FUNCTION,
                COLLECTORS
            )
            .addStatement(
                """
                final $T updatedFreq = $T.requireNonNullElse(updated, $T.<$T>of()).stream()
                    .collect($T.groupingBy($T.identity(), $T.counting()))
                """.trim(),
                mapPtn,
                OBJECTS,
                LIST,
                acc.unNestedPrimaryTypeName(),
                COLLECTORS,
                FUNCTION,
                COLLECTORS
            );

        final ParameterizedTypeName listPtn = ParameterizedTypeName.get(LIST, acc.unNestedPrimaryTypeName());
        builder.addStatement("final $T added = new $T<>()", listPtn, ARRAY_LIST)
            .addStatement("final $T removed = new $T<>()", listPtn, ARRAY_LIST)
            .addStatement("final $T common = new $T<>()", listPtn, ARRAY_LIST);

        final ParameterizedTypeName setPtn = ParameterizedTypeName.get(SET, acc.unNestedPrimaryTypeName());
        builder.addComment("Obtain unique elements")
            .addStatement("final $T allUniqueElements = new $T<>(originalFreq.keySet())", setPtn, HASH_SET)
            .addStatement("allUniqueElements.addAll(updatedFreq.keySet())");

        builder.beginControlFlow("for (final $T element : allUniqueElements)", acc.unNestedPrimaryTypeName())
            .addStatement("final long originalCount = originalFreq.getOrDefault(element, 0L)")
            .addStatement("final long updatedCount = updatedFreq.getOrDefault(element, 0L)")
            .addStatement("final long commonCount = $T.min(originalCount, updatedCount)", MATH)
            
            .beginControlFlow("for (long i = 0; i < commonCount; i++)")
            .addStatement("common.add(element)")
            .endControlFlow()

            .beginControlFlow("if (originalCount > updatedCount)")
            .addStatement("final long removedCount = originalCount - updatedCount")
            .beginControlFlow("for (long i = 0; i < removedCount; i++)")
            .addStatement("removed.add(element)")
            .endControlFlow()
            .endControlFlow()

            .beginControlFlow("if (updatedCount > originalCount)")
            .addStatement("final long addedCount = updatedCount - originalCount")
            .beginControlFlow("for (long i = 0; i < addedCount; i++)")
            .addStatement("added.add(element)")
            .endControlFlow()
            .endControlFlow()

            .endControlFlow();
        
        // Constructor is added, common, removed
        builder.addStatement(
            "return new $T($T.copyOf(added), $T.copyOf(common), $T.copyOf(removed))", 
            collectionDiffRecord(acc).className(),
            LIST, LIST, LIST
        );
        return true;
    }

    private void methodParamsReturn(AnalysedComponent analysedComponent, AnalysedCollectionComponent acc,
            final MethodSpec.Builder builder) {
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        builder.addModifiers(Modifier.FINAL, Modifier.STATIC)
                .returns(collectionDiffRecord(acc).className())
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

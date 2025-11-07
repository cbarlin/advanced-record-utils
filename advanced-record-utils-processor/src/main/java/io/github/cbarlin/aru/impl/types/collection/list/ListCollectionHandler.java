package io.github.cbarlin.aru.impl.types.collection.list;

import io.github.cbarlin.aru.impl.types.collection.StandardCollectionHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARRAY_LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTORS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.HASH_SET;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;
import static io.github.cbarlin.aru.impl.Constants.Names.FUNCTION;
import static io.github.cbarlin.aru.impl.Constants.Names.LONG;
import static io.github.cbarlin.aru.impl.Constants.Names.MAP;
import static io.github.cbarlin.aru.impl.Constants.Names.MATH;

public abstract class ListCollectionHandler extends StandardCollectionHandler {

    protected ListCollectionHandler(final ClassName classNameOnComponent, final ClassName mutableClassName) {
        super(classNameOnComponent, mutableClassName, LIST);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String targetVariableName, final TypeName innerTypeName) {
        methodBuilder
            .addComment("No immutable version exists - returning original object")
            .addStatement("final $T<$T> $L = $L", classNameOnComponent, innerTypeName, targetVariableName, fieldName);
    }

    @Override
    public void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord) {
        final ParameterizedTypeName mapPtn = ParameterizedTypeName.get(MAP, innerType, LONG);
        final ParameterizedTypeName listPtn = ParameterizedTypeName.get(LIST, innerType);
        final ParameterizedTypeName setPtn = ParameterizedTypeName.get(SET, innerType);
        methodBuilder.addModifiers(Modifier.FINAL, Modifier.STATIC)
            .returns(collectionResultRecord)
            .addParameter(
                ParameterSpec.builder(listPtn, "original", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .build()   
            )
            .addParameter(
                ParameterSpec.builder(listPtn, "updated", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .build()   
            )
            .addComment("Create frequency maps to count occurrences")
            .addStatement(
                """
                final $T originalFreq = $T.requireNonNullElse(original, $T.<$T>of()).stream()
                    .collect($T.groupingBy($T.identity(), $T.counting()))
                """.trim(),
                mapPtn,
                OBJECTS,
                LIST,
                innerType,
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
                innerType,
                COLLECTORS,
                FUNCTION,
                COLLECTORS
            )
            .addStatement("final $T added = new $T<>()", listPtn, ARRAY_LIST)
            .addStatement("final $T removed = new $T<>()", listPtn, ARRAY_LIST)
            .addStatement("final $T common = new $T<>()", listPtn, ARRAY_LIST)
            .addComment("Obtain unique elements")
            .addStatement("final $T allUniqueElements = new $T<>(originalFreq.keySet())", setPtn, HASH_SET)
            .addStatement("allUniqueElements.addAll(updatedFreq.keySet())")
            .beginControlFlow("for (final $T element : allUniqueElements)", innerType)
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

            .endControlFlow()
            .addStatement(
                "return new $T($T.copyOf(added), $T.copyOf(common), $T.copyOf(removed))", 
                collectionResultRecord,
                LIST, LIST, LIST
            );
    }
}

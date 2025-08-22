package io.github.cbarlin.aru.impl.types.collection.set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTORS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;
import static io.github.cbarlin.aru.impl.Constants.Names.PREDICATE;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.impl.types.collection.StandardCollectionHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

public abstract class SetCollectionHandler extends StandardCollectionHandler {

    protected SetCollectionHandler(final ClassName classNameOnComponent, final ClassName mutableClassName) {
        super(classNameOnComponent, mutableClassName, SET);
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder
            .beginControlFlow("if ($T.isNull($L))", OBJECTS, fieldName)
            .addStatement("return $T.of()", SET)
            .endControlFlow()
            .addStatement(
                "final $T<$T> $L = $L.stream()\n    .filter($T::nonNull)\n    .collect($T.toUnmodifiableSet())",
                immutableClassName,
                innerTypeName,
                assignmentName,
                fieldName,
                OBJECTS,
                COLLECTORS
            );
    }

    @Override
    public void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder builder, final ClassName collectionResultRecord) {
        final ParameterizedTypeName setPtn = ParameterizedTypeName.get(SET, innerType);
        builder
            .addParameter(
                ParameterSpec.builder(setPtn, "original", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .build()   
            )
            .addParameter(
                ParameterSpec.builder(setPtn, "updated", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .build()   
            )
            .returns(collectionResultRecord)
            .addComment("Filter elements by comparing against the other set")
            .addStatement("final $T nUpdated = $T.requireNonNullElse(updated, $T.of())", setPtn, OBJECTS, SET)
            .addStatement("final $T nOriginal = $T.requireNonNullElse(original, $T.of())", setPtn, OBJECTS, SET)
            .addStatement(
                """
                    final $T added = nUpdated.stream()
                        .filter($T.not(nOriginal::contains))
                        .collect($T.toUnmodifiableSet())
                """.trim(),
                setPtn,
                PREDICATE,
                COLLECTORS
            )
            .addStatement(
                """
                    final $T removed = nOriginal.stream()
                        .filter($T.not(nUpdated::contains))
                        .collect($T.toUnmodifiableSet())
                """.trim(),
                setPtn,
                PREDICATE,
                COLLECTORS
            )
            .addStatement(
                """
                    final $T common = nOriginal.stream()
                        .filter(nUpdated::contains)
                        .collect($T.toUnmodifiableSet())
                """.trim(),
                setPtn,
                COLLECTORS
            );
        // Constructor is added, common, removed
        builder.addStatement("return new $T(added, common, removed)", collectionResultRecord);
    }

}

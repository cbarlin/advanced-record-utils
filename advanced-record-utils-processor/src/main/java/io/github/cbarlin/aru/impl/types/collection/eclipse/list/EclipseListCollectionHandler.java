package io.github.cbarlin.aru.impl.types.collection.eclipse.list;

import io.github.cbarlin.aru.impl.types.collection.eclipse.EclipseCollectionHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.LONG;
import static io.github.cbarlin.aru.impl.Constants.Names.MATH;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS_MAP_ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__LISTS_FACTORY;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_SET;

public abstract sealed class EclipseListCollectionHandler extends EclipseCollectionHandler permits EclipseImmutableList, EclipseMutableList {

    public static final String FREQUENCY_MAP = """
        final $T $L = $T.requireNonNullElse($L, $T.immutable.<$T>of())
            .aggregateBy(a -> a, () -> 0l, (c, ign) -> c + 1l)
        """.trim();

    protected EclipseListCollectionHandler(final ClassName classNameOnComponent) {
        super(classNameOnComponent, ECLIPSE_COLLECTIONS__MUTABLE_LIST, ECLIPSE_COLLECTIONS__IMMUTABLE_LIST, ECLIPSE_COLLECTIONS__LISTS_FACTORY);
    }

    @Override
    public final void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord) {
        final ParameterizedTypeName mapPtn = ParameterizedTypeName.get(ECLIPSE_COLLECTIONS_MAP_ITERABLE, innerType, LONG);
        final ParameterizedTypeName listPtn = ParameterizedTypeName.get(ECLIPSE_COLLECTIONS__MUTABLE_LIST, innerType);
        final ParameterizedTypeName setPtn = ParameterizedTypeName.get(ECLIPSE_COLLECTIONS__MUTABLE_SET, innerType);
        final ParameterizedTypeName componentPtn = ParameterizedTypeName.get(classNameOnComponent, innerType);
        methodBuilder.addModifiers(Modifier.FINAL, Modifier.STATIC)
                .returns(collectionResultRecord)
                .addParameter(
                        ParameterSpec.builder(componentPtn, "original", Modifier.FINAL)
                                .addAnnotation(NULLABLE)
                                .build()
                )
                .addParameter(
                        ParameterSpec.builder(componentPtn, "updated", Modifier.FINAL)
                                .addAnnotation(NULLABLE)
                                .build()
                )
                .addComment("Create frequency maps to count occurrences")
                .addStatement(
                    FREQUENCY_MAP,
                    mapPtn,
                    "originalFreq",
                    OBJECTS,
                    "original",
                    factoryClassName,
                    innerType
                )
                .addStatement(
                    FREQUENCY_MAP,
                    mapPtn,
                    "updatedFreq",
                    OBJECTS,
                    "updated",
                    factoryClassName,
                    innerType
                )
                .addStatement("final $T added = $T.mutable.<$T>of()", listPtn, ECLIPSE_COLLECTIONS__LISTS_FACTORY, innerType)
                .addStatement("final $T removed = $T.mutable.<$T>of()", listPtn, ECLIPSE_COLLECTIONS__LISTS_FACTORY, innerType)
                .addStatement("final $T common = $T.mutable.<$T>of()", listPtn, ECLIPSE_COLLECTIONS__LISTS_FACTORY, innerType)
                .addComment("Obtain unique elements")
                .addStatement("final $T allUniqueElements = originalFreq.keysView().toSet()", setPtn)
                .addStatement("allUniqueElements.addAll(updatedFreq.keysView().toSet())")
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
                   "return new $T(added.toImmutableList(), common.toImmutableList(), removed.toImmutableList())",
                    collectionResultRecord
                );
    }

}

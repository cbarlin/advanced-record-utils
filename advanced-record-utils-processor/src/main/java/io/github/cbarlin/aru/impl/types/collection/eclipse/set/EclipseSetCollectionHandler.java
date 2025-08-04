package io.github.cbarlin.aru.impl.types.collection.eclipse.set;

import io.github.cbarlin.aru.impl.types.collection.eclipse.EclipseCollectionHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_SET;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_SET;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__SETS_FACTORY;

public abstract sealed class EclipseSetCollectionHandler extends EclipseCollectionHandler permits EclipseImmutableSet, EclipseMutableSet, EclipseMultiReaderSet {

    protected EclipseSetCollectionHandler(ClassName classNameOnComponent) {
        super(classNameOnComponent, ECLIPSE_COLLECTIONS__MUTABLE_SET, ECLIPSE_COLLECTIONS__IMMUTABLE_SET, ECLIPSE_COLLECTIONS__SETS_FACTORY);
    }

    @Override
    public final void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder builder, final ClassName collectionResultRecord) {
        final ParameterizedTypeName setPtn = ParameterizedTypeName.get(classNameOnComponent, innerType);
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
                .beginControlFlow("if ($T.nonNull(original) && $T.nonNull(updated))", OBJECTS, OBJECTS)
                .addStatement(
                        "final $T<$T> og = original.toImmutableSet()",
                        immutableClassName, innerType
                )
                .addStatement(
                        "final $T<$T> upd = updated.toImmutableSet()",
                        immutableClassName, innerType
                )
                .addStatement(
                        "return new $T(upd.difference(og), og.intersect(upd), og.difference(upd))",
                        collectionResultRecord
                )
                .nextControlFlow("else if ($T.nonNull(original))", OBJECTS, OBJECTS)
                .addStatement(
                        "return new $T(original.toImmutableSet(), $T.immutable.of(), $T.immutable.of())",
                        collectionResultRecord,
                        factoryClassName, factoryClassName
                )
                .nextControlFlow("else if ($T.nonNull(updated))", OBJECTS, OBJECTS)
                .addStatement(
                        "return new $T(updated.toImmutableSet(), $T.immutable.of(), $T.immutable.of())",
                        collectionResultRecord,
                        factoryClassName, factoryClassName
                )
                .endControlFlow()
                .addStatement(
                        "return new $T($T.immutable.of(), $T.immutable.of(), $T.immutable.of())",
                        collectionResultRecord,
                        factoryClassName, factoryClassName, factoryClassName
                );
    }
}

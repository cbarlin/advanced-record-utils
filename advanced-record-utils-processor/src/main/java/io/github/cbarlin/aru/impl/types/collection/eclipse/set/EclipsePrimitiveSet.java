package io.github.cbarlin.aru.impl.types.collection.eclipse.set;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.collection.eclipse.EclipseCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.PrimitiveHelper;
import io.micronaut.sourcegen.javapoet.*;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

public final class EclipsePrimitiveSet extends EclipseCollectionHandler {

    private static final AnnotationSpec ANNO_NULLABLE = AnnotationSpec.builder(NULLABLE).build();

    public EclipsePrimitiveSet(
        final ClassName classNameOnComponent,
        final ClassName mutableClassName,
        final ClassName immutableClassName,
        final ClassName factoryClassName
    ) {
        super(classNameOnComponent, mutableClassName, immutableClassName, factoryClassName, mutableClassName);
    }

    @Override
    public void writeNullableAutoRemoveManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleRemoveMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        PrimitiveHelper.writeNullableRemoveMany(
                component, builder, innerType, addAllMethodName, visitor
        );
    }

    @Override
    public void writeNullableImmutableRemoveManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleRemoveMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        PrimitiveHelper.writeNullableRemoveMany(
                component, builder, innerType, addAllMethodName, visitor
        );
    }

    @Override
    public void writeNullableImmutableAddManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleAddMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        PrimitiveHelper.writeNullableAdders(
                component, builder, innerType, addAllMethodName, visitor, factoryClassName
        );
    }

    @Override
    public void writeNullableAutoAddManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleAddMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        PrimitiveHelper.writeNullableAdders(
                component, builder, innerType, addAllMethodName, visitor, factoryClassName
        );
    }

    @Override
    public void writeNonNullAutoRemoveManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleRemoveMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        PrimitiveHelper.writeNonNullRemoveMany(component, builder, innerType, addAllMethodName, visitor);
    }

    @Override
    public void writeNonNullImmutableRemoveManyToBuilder(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String singleRemoveMethodName,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        PrimitiveHelper.writeNonNullRemoveMany(component, builder, innerType, addAllMethodName, visitor);
    }

    @Override
    protected void writeNonNullableEclipseAdders(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String singleAddMethodName,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        PrimitiveHelper.writeNonNullAdders(component, builder, innerType, addAllMethodName, visitor);
    }

    @Override
    public boolean canHandle(final AnalysedComponent component) {
        return component.className()
                .filter(classNameOnComponent::equals)
                .isPresent();
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent ecc, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final FieldSpec fSpec = FieldSpec.builder(mutableClassName, ecc.name(), Modifier.PRIVATE)
                                         .addAnnotation(NON_NULL)
                                         .initializer("$T.mutable.empty()", factoryClassName)
                                         .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    public void addNullableAutoField(final AnalysedComponent ecc, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final FieldSpec fSpec = FieldSpec.builder(mutableClassName, ecc.name(), Modifier.PRIVATE)
                .addAnnotation(NULLABLE)
                .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    protected final void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        if (classNameOnComponent.equals(mutableClassName)) {
            methodBuilder
                .addComment("Created in $L", this.getClass().getCanonicalName())
                .addStatement("final $T $L = $T.mutable.ofAll($L)", mutableClassName, assignmentName, factoryClassName, fieldName);
        } else {
            methodBuilder
                .addComment("Created in $L", this.getClass().getCanonicalName())
                .addStatement("final $T $L = $L.toImmutable()", immutableClassName, assignmentName, fieldName);
        }
    }

    @Override
    public void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        PrimitiveHelper.writeNonNullAutoSetter(component, methodBuilder, nullReplacesNotNull);
    }

    @Override
    public final void writeMergerMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder) {
        PrimitiveHelper.writeMergerMethod(classNameOnComponent, methodBuilder, mutableClassName, factoryClassName);
    }

    @Override
    public final void addDiffRecordComponents(final TypeName innerType, final ToBeBuiltRecord recordBuilder) {
        PrimitiveHelper.addDiffRecordComponents(immutableClassName, recordBuilder);
    }

    @Override
    public final void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder builder, final ClassName collectionResultRecord) {
        builder
            .addParameter(
                ParameterSpec.builder(classNameOnComponent.annotated(ANNO_NULLABLE), "original", Modifier.FINAL)
                             .build()
            )
            .addParameter(
                ParameterSpec.builder(classNameOnComponent.annotated(ANNO_NULLABLE), "updated", Modifier.FINAL)
                             .build()
            )
            .returns(collectionResultRecord)
            .addComment("Filter elements by comparing against the other set")
            .beginControlFlow("if ($T.nonNull(original) && $T.nonNull(updated))", OBJECTS, OBJECTS)
            .addStatement(
                "final $T og = original.toImmutable()",
                immutableClassName
            )
            .addStatement(
                "final $T upd = updated.toImmutable()",
                immutableClassName
            )
            .addStatement(
                "return new $T(upd.difference(og), og.intersect(upd), og.difference(upd))",
                collectionResultRecord
            )
            .nextControlFlow("else if ($T.nonNull(original))", OBJECTS)
            .addStatement(
                "return new $T($T.immutable.of(), $T.immutable.of(), original.toImmutable())",
                collectionResultRecord,
                factoryClassName, factoryClassName
            )
            .nextControlFlow("else if ($T.nonNull(updated))", OBJECTS)
            .addStatement(
                "return new $T(updated.toImmutable(), $T.immutable.of(), $T.immutable.of())",
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

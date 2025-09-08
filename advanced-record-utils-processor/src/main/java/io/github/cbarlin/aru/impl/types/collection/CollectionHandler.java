package io.github.cbarlin.aru.impl.types.collection;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.ITERABLE;
import static io.github.cbarlin.aru.impl.Constants.Names.ITERATOR;
import static io.github.cbarlin.aru.impl.Constants.Names.SPLITERATOR;

/**
 * A class that knows how best to handle a collection
 * <p>
 * This class provides a comprehensive API for handling collections with different
 *   characteristics: nullable vs non-nullable, and auto (mutable) vs immutable variants.
 * <p>
 * Each combination has dedicated methods to ensure type safety and proper handling.
 */
public abstract class CollectionHandler {

    /**
     * Determine if this handler can handle this collection type
     */
    public abstract boolean canHandle(final AnalysedComponent component);

    //#region Nullable, Auto

    /**
     * Add a nullable field that inherits the type passed into it
     * <p>
     * Does not write returns or params
     */
    public abstract void addNullableAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType);

    /**
     * Write the "getter" in the builder that is nullable and inherits the type passed to it
     */
    public void writeNullableAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.addAnnotation(NULLABLE)
            .addStatement("return this.$L", component.name())
            .returns(component.typeName());
    }

    /**
     * Write the "setter" in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull);

    /**
     * Write an "add" method in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write "addAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public void writeNullableAutoAddManyToBuilder(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeLoopDelegatingMultipleOperators(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

    /**
     * Write "removeAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public void writeNullableAutoRemoveManyToBuilder(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String singleRemoveMethodName,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeLoopDelegatingMultipleOperators(component, builder, innerType, singleRemoveMethodName, addAllMethodName, visitor);
    }

    /**
     * Write a "remove" method in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write a "removeIf" method in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write a "retainAll" method in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    //#endregion
    //#region Nullable, Immutable

    /**
     * Add a nullable field that will be result in an immutable output
     * <p>
     * Does not write returns or params
     */
    public abstract void addNullableImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType);

    /**
     * Write the "getter" in the builder that is nullable and results in an immutable result
     */
    public abstract void writeNullableImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "setter" in the builder that is nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull);

    /**
     * Write an "add" method in the builder that is nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write a "remove" method in the builder that is nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableImmutableRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write a "removeIf" method in the builder that is nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableImmutableRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write a "retainAll" method in the builder that is nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableImmutableRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write "addAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public void writeNullableImmutableAddManyToBuilder(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeLoopDelegatingMultipleOperators(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

    /**
     * Write "removeAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public void writeNullableImmutableRemoveManyToBuilder(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String singleRemoveMethodName,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeLoopDelegatingMultipleOperators(component, builder, innerType, singleRemoveMethodName, addAllMethodName, visitor);
    }

    //#endregion
    //#region Non-Null, Auto

    /**
     * Add a non-nullable field that inherits the type passed into it
     * <p>
     * Does not write returns or params
     */
    public abstract void addNonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType);

    /**
     * Write the "getter" in the builder that is never nullable, but inherits the type passed to it
     */
    public abstract void writeNonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "setter" in the builder that is never nullable, but inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull);

    /**
     * Write the "add" method in the builder that is never nullable, but inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "remove" method in the builder that is never nullable, but inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "removeIf" method in the builder that is never nullable, but inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "retainAll" method in the builder that is never nullable, but inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write "addAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public void writeNonNullAutoAddManyToBuilder(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeNonNullCollectionAdder(component, builder, innerType, addAllMethodName, visitor);
        writeLoopDelegatingMultipleOperators(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

    /**
     * Write "removeAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public void writeNonNullAutoRemoveManyToBuilder(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String singleRemoveMethodName,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeNonNullCollectionRemoveAll(component, builder, innerType, addAllMethodName, visitor);
        writeLoopDelegatingMultipleOperators(component, builder, innerType, singleRemoveMethodName, addAllMethodName, visitor);
    }

    //#endregion
    //#region Non-Null, Immutable

    /**
     * Add a non-nullable field that will result in an immutable output
     * <p>
     * Does not write returns or params
     */
    public abstract void addNonNullImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType);

    /**
     * Write the "getter" in the builder that is never nullable and results in an immutable result
     */
    public abstract void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "setter" in the builder that is never nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull);

    /**
     * Write the "add" method in the builder that is never nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "remove" method in the builder that is never nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullImmutableRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "removeIf" method in the builder that is never nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullImmutableRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write the "retainAll" method in the builder that is never nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullImmutableRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

    /**
     * Write "addAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public void writeNonNullImmutableAddManyToBuilder(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeNonNullCollectionAdder(component, builder, innerType, addAllMethodName, visitor);
        writeLoopDelegatingMultipleOperators(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

    /**
     * Write "removeAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public void writeNonNullImmutableRemoveManyToBuilder(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String singleRemoveMethodName,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeNonNullCollectionRemoveAll(component, builder, innerType, addAllMethodName, visitor);
        writeLoopDelegatingMultipleOperators(component, builder, innerType, singleRemoveMethodName, addAllMethodName, visitor);
    }

    //#endregion
    //#region Merger, Differ

    /**
     * Write the method that merges two instances of this collection
     * <p>
     * Will write the params and return type
     */
    public abstract void writeMergerMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder);

    /**
     * Write the method that creates the diff collection result
     * <p>
     * Will write the params and return type
     */
    public abstract void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord);

    /**
     * Will add components to the record for this type of collection
     */
    public abstract void addDiffRecordComponents(final TypeName innerType, final ToBeBuiltRecord recordBuilder);

    //#endregion

    //#region Internal defaults that defer to the single adder

    protected static void writeNonNullCollectionAdder (
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(COLLECTION, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
            .addJavadoc("A collection to be merged into the collection")
            .addAnnotation(NOT_NULL)
            .build();
        final MethodSpec.Builder method = builder
            .createMethod(addAllMethodName, visitor.claimableOperation(), COLLECTION)
            .addJavadoc("Adds all elements of the provided collection to {@code $L}", fieldName)
            .addParameter(param)
            .returns(builder.className())
            .addAnnotation(NOT_NULL)
            .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
            .addStatement("this.$L.addAll($L)", fieldName, fieldName)
            .endControlFlow()
            .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    protected static void writeNonNullCollectionRemoveAll (
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(COLLECTION, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
            .addJavadoc("A collection to be merged into the collection")
            .addAnnotation(NOT_NULL)
            .build();
        final MethodSpec.Builder method = builder
            .createMethod(addAllMethodName, visitor.claimableOperation(), COLLECTION)
            .addJavadoc("Adds all elements of the provided collection to {@code $L}", fieldName)
            .addParameter(param)
            .returns(builder.className())
            .addAnnotation(NOT_NULL)
            .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
            .addStatement("this.$L.removeAll($L)", fieldName, fieldName)
            .endControlFlow()
            .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    protected static void writeLoopDelegatingMultipleOperators(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String singleOperatorMethodName,
        final String methodName,
        final AruVisitor<?> visitor
    ) {
        writeBasicIterableOperator(component, builder, innerType, singleOperatorMethodName, methodName, visitor);
        writeBasicIteratorOperator(component, builder, innerType, singleOperatorMethodName, methodName, visitor);
        writeBasicSpliteratorOperator(component, builder, innerType, singleOperatorMethodName, methodName, visitor);
    }

    protected static void writeBasicIterableOperator(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleOperatorMethodName,
        final String methodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(ITERABLE, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
            .addJavadoc("An iterable to be operated on with the collection")
            .addAnnotation(NOT_NULL)
            .build();
        final MethodSpec.Builder method = builder
                .createMethod(methodName, visitor.claimableOperation(), ITERABLE)
                .addJavadoc("Operates on all elements of the provided iterable with {@code $L}", fieldName)
                .addParameter(param)
                .returns(builder.className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
                .beginControlFlow("for (final $T __single : $L)", innerType, fieldName)
                .addStatement("this.$L(__single)", singleOperatorMethodName)
                .endControlFlow()
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    protected static void writeBasicIteratorOperator(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleOperatorMethodName,
        final String methodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(ITERATOR, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
            .addJavadoc("An iterator to be operated on with the collection")
            .addAnnotation(NOT_NULL)
            .build();
        final MethodSpec.Builder method = builder
                .createMethod(methodName, visitor.claimableOperation(), ITERATOR)
                .addJavadoc("Operates on all elements of the provided iterator with {@code $L}", fieldName)
                .addParameter(param)
                .returns(builder.className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
                .beginControlFlow("while($L.hasNext())", fieldName)
                .addStatement("this.$L($L.next())", singleOperatorMethodName, fieldName)
                .endControlFlow()
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    protected static void writeBasicSpliteratorOperator(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleOperatorMethodName,
        final String methodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(SPLITERATOR, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
            .addJavadoc("A spliterator to be operated on with the collection")
            .addAnnotation(NOT_NULL)
            .build();
        final MethodSpec.Builder method = builder
                .createMethod(methodName, visitor.claimableOperation(), SPLITERATOR)
                .addJavadoc("Operates on all elements of the provided spliterator with {@code $L}", fieldName)
                .addParameter(param)
                .returns(builder.className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
                .addStatement("$L.forEachRemaining(this::$L)", fieldName, singleOperatorMethodName)
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    //#endregion
}

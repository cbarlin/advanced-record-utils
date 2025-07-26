package io.github.cbarlin.aru.impl.types.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.COLLECTION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.ITERABLE;
import static io.github.cbarlin.aru.impl.Constants.Names.ITERATOR;
import static io.github.cbarlin.aru.impl.Constants.Names.SPLITERATOR;

import javax.lang.model.element.Modifier;

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
    public abstract void writeNullableAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNonNull);

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
        writeBasicAdders(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

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
    public abstract void writeNullableImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNonNull);

    /**
     * Write an "add" method in the builder that is nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNullableImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

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
        writeBasicAdders(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
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
    public abstract void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNonNull);

    /**
     * Write the "add" method in the builder that is never nullable, but inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

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
        writeBasicAdders(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
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
    public abstract void writeNonNullImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNonNull);

    /**
     * Write the "add" method in the builder that is never nullable and results in an immutable result
     * <p>
     * Does not write returns or params
     */
    public abstract void writeNonNullImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType);

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
        writeBasicAdders(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
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

    protected static void writeBasicAdders(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        writeBasicCollectionAdder(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
        writeBasicIterableAdder(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
        writeBasicIteratorAdder(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
        writeBasicSpliteratorAdder(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

    protected static void writeBasicCollectionAdder(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
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

    protected static void writeBasicIterableAdder(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(ITERABLE, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
            .addJavadoc("An iterable to be merged into the collection")
            .addAnnotation(NOT_NULL)
            .build();
        final MethodSpec.Builder method = builder
                .createMethod(addAllMethodName, visitor.claimableOperation(), ITERABLE)
                .addJavadoc("Adds all elements of the provided iterable to {@code $L}", fieldName)
                .addParameter(param)
                .returns(builder.className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
                .beginControlFlow("for (final $T __addable : $L)", innerType, fieldName)
                .addStatement("this.$L(__addable)", singleAddMethodName)
                .endControlFlow()
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    protected static void writeBasicIteratorAdder(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(ITERATOR, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
            .addJavadoc("An iterator to be merged into the collection")
            .addAnnotation(NOT_NULL)
            .build();
        final MethodSpec.Builder method = builder
                .createMethod(addAllMethodName, visitor.claimableOperation(), ITERATOR)
                .addJavadoc("Adds all elements of the provided iterator to {@code $L}", fieldName)
                .addParameter(param)
                .returns(builder.className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
                .beginControlFlow("while($L.hasNext())", fieldName)
                .addStatement("this.$L($L.next())", singleAddMethodName, fieldName)
                .endControlFlow()
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    protected static void writeBasicSpliteratorAdder(
        final AnalysedComponent component, 
        final ToBeBuilt builder, 
        final TypeName innerType, 
        final String singleAddMethodName, 
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(SPLITERATOR, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
            .addJavadoc("A spliterator to be merged into the collection")
            .addAnnotation(NOT_NULL)
            .build();
        final MethodSpec.Builder method = builder
                .createMethod(addAllMethodName, visitor.claimableOperation(), SPLITERATOR)
                .addJavadoc("Adds all elements of the provided spliterator to {@code $L}", fieldName)
                .addParameter(param)
                .returns(builder.className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
                .addStatement("$L.forEachRemaining(this::$L)", fieldName, singleAddMethodName)
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    //#endregion
}

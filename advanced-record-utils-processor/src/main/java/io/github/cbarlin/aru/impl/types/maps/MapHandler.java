package io.github.cbarlin.aru.impl.types.maps;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

/**
 * An interface for handlers of various Map types
 * <p>
 * This class provides a comprehensive API for handling maps with different
 *   characteristics: nullable vs non-nullable, and auto (mutable) vs immutable variants.
 * <p>
 * Each combination has dedicated methods to ensure type safety and proper handling.
 */
public interface MapHandler {

    /**
     * Determine if this handler can handle this collection type
     */
    boolean canHandle(final AnalysedComponent analysedComponent);

    /**
     * Extract the primary type of the component.
     * Must be able to do so if {@link #canHandle(AnalysedComponent)} is true
     */
    TypeName extractKeyType(final AnalysedComponent analysedComponent);

    /**
     * Extract the secondary type of the component.
     * Must be able to do so if {@link #canHandle(AnalysedComponent)} is true
     */
    TypeName extractValueType(final AnalysedComponent analysedComponent);

    // region nullable, auto

    /**
     * Add a nullable field that inherits the type passed into it
     * <p>
     * Does not write returns or params
     */
    void addNullableAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType);

    /**
     * Write the "getter" in the builder that is nullable and inherits the type passed to it
     */
    void writeNullableAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType);

    /**
     * Write the "setter" in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
     void writeNullableAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull);

    /**
     * Write an "add" method in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType);

    /**
     * Write a "remove" method in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType);

    /**
     * Write any other specialisations that are relevant to the current Map implementation
     */
    default void writeNullableAutoSpecialisedMethods(final AnalysedComponent component, final AruVisitor<?> visitor, final ToBeBuilt toBeBuilt, final TypeName keyType, final TypeName valueType) {
        // No-op
    }

    // endregion

    // region nullable, immutable

    /**
     * Add a nullable field that inherits the type passed into it
     * <p>
     * Does not write returns or params
     */
    void addNullableImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType);

    /**
     * Write the "getter" in the builder that is nullable and inherits the type passed to it
     */
    void writeNullableImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType);

    /**
     * Write the "setter" in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNullableImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull);

    /**
     * Write an "add" method in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNullableImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType);

    /**
     * Write a "remove" method in the builder that is nullable and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNullableImmutableRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType);

    /**
     * Write any other specialisations that are relevant to the current Map implementation
     */
    default void writeNullableImmutableSpecialisedMethods(final AnalysedComponent component, final AruVisitor<?> visitor, final ToBeBuilt toBeBuilt, final TypeName keyType, final TypeName valueType) {
        // No-op
    }

    // endregion

    // region nonNull, auto

    /**
     * Add a nonNull field that inherits the type passed into it
     * <p>
     * Does not write returns or params
     */
    void addNonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType);

    /**
     * Write the "getter" in the builder that is nonNull and inherits the type passed to it
     */
    void writeNonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType);

    /**
     * Write the "setter" in the builder that is nonNull and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull);

    /**
     * Write an "add" method in the builder that is nonNull and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNonNullAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType);

    /**
     * Write a "remove" method in the builder that is nonNull and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNonNullAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType);

    /**
     * Write any other specialisations that are relevant to the current Map implementation
     */
    default void writeNonNullAutoSpecialisedMethods(final AnalysedComponent component, final AruVisitor<?> visitor, final ToBeBuilt toBeBuilt, final TypeName keyType, final TypeName valueType) {
        // No-op
    }

    // endregion

    // region nonNull, immutable

    /**
     * Add a nonNull field that inherits the type passed into it
     * <p>
     * Does not write returns or params
     */
    void addNonNullImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType);

    /**
     * Write the "getter" in the builder that is nonNull and inherits the type passed to it
     */
    void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType);

    /**
     * Write the "setter" in the builder that is nonNull and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNonNullImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull);

    /**
     * Write an "add" method in the builder that is nonNull and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNonNullImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType);

    /**
     * Write a "remove" method in the builder that is nonNull and inherits the type passed to it
     * <p>
     * Does not write returns or params
     */
    void writeNonNullImmutableRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType);

    /**
     * Write any other specialisations that are relevant to the current Map implementation
     */
    default void writeNonNullImmutableSpecialisedMethods(final AnalysedComponent component, final AruVisitor<?> visitor, final ToBeBuilt toBeBuilt, final TypeName keyType, final TypeName valueType) {
        // No-op
    }

    // endregion

    //#region Merger, Differ

    /**
     * Write the method that merges two instances of this collection
     * <p>
     * Will write the params and return type
     */
    void writeMergerMethod(final TypeName keyType, final TypeName valueType, final MethodSpec.Builder methodBuilder);

    /**
     * Write the method that creates the diff collection result
     * <p>
     * Will write the params and return type
     */
    void writeDifferMethod(final TypeName keyType, final TypeName valueType, final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord);

    /**
     * Will add components to the record for this type of collection
     */
    void addDiffRecordComponents(final TypeName keyType, final TypeName valueType, final ToBeBuiltRecord recordBuilder);

    //#endregion
}

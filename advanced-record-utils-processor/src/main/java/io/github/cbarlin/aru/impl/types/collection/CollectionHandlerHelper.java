package io.github.cbarlin.aru.impl.types.collection;

import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.collection.wrapper.NonNullAutoCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.wrapper.NonNullImmutableNullCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.wrapper.NullableAutoCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.wrapper.NullableImmutableCollectionHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

/**
 * A wrapper around a {@link io.github.cbarlin.aru.impl.types.collection.CollectionHandler} that
 *   has already had the null/not-null, mutable/immutable, and null-replace/null-noop settings applied
 */
public sealed interface CollectionHandlerHelper permits 
    NonNullAutoCollectionHandler,
    NonNullImmutableNullCollectionHandler,
    NullableAutoCollectionHandler,
    NullableImmutableCollectionHandler
{

    boolean nullReplacesNotNull();

    /**
     * The component this is working with
     */
    AnalysedComponent component();

    /**
     * Adds the appropriate field type, including annotations
     */
    void addField(final ToBeBuilt addFieldTo);

    /**
     * Write the getter
     * <p>
     * Writes the return type and annotation, 
     *   but not the generated annotation
     */
    void writeGetter(final MethodSpec.Builder methodBuilder);

    /**
     * Write the setter
     * <p>
     * Does not write the returns or the params
     */
    void writeSetter(final MethodSpec.Builder methodBuilder);

    /**
     * Write the "add single"
     * <p>
     * Does not write the returns or the params
     */
    void writeAddSingle(final MethodSpec.Builder methodBuilder);

    /**
     * Write "addAll" methods to the builder
     */
    void writeAddMany(
        final BuilderClass builderClass,
        final String singleAdderName,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    );

    /**
     * Write the method that merges two instances of this collection
     * <p>
     * Will write the params and return type
     */
    void writeMergerMethod(final MethodSpec.Builder methodBuilder);

    /**
     * Write the method that creates the diff collection result
     * <p>
     * Will write the params and return type
     */
    void writeDifferMethod(final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord);

    /**
     * Will add components to the record for this type of collection
     */
    void addDiffRecordComponents(final ToBeBuiltRecord recordBuilder);
}

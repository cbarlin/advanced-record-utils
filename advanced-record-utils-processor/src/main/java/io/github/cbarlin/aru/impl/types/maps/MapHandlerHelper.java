package io.github.cbarlin.aru.impl.types.maps;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.maps.wrapper.NonNullAutoMapHandler;
import io.github.cbarlin.aru.impl.types.maps.wrapper.NonNullImmutableMapHandler;
import io.github.cbarlin.aru.impl.types.maps.wrapper.NullableAutoMapHandler;
import io.github.cbarlin.aru.impl.types.maps.wrapper.NullableImmutableMapHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

/**
 * A wrapper around a {@link io.github.cbarlin.aru.impl.types.maps.MapHandler} that
 *   has already had the null/not-null, mutable/immutable, and null-replace/null-noop settings applied
 */
public sealed interface MapHandlerHelper
    permits NonNullAutoMapHandler,
        NonNullImmutableMapHandler,
        NullableAutoMapHandler,
        NullableImmutableMapHandler
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
     * Write the "remove single"
     * <p>
     * Does not write the returns or the params
     */
    void writeRemoveSingle(final MethodSpec.Builder methodBuilder);

    /**
     * Write any other specialisations that are relevant to the current Map implementation
     */
    void writeSpecialisedMethods(final ToBeBuilt toBeBuilt, final AruVisitor<?> visitor);

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

package io.github.cbarlin.aru.impl.types.maps.wrapper;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.maps.MapHandler;
import io.github.cbarlin.aru.impl.types.maps.MapHandlerHelper;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

public record NonNullImmutableMapHandler(
    AnalysedComponent component,
    TypeName keyType,
    TypeName valueType,
    MapHandler handler,
    boolean nullReplacesNotNull
) implements MapHandlerHelper {
    public NonNullImmutableMapHandler(
            final AnalysedComponent component,
            final MapHandler handler,
            final boolean nullReplacesNotNull
    ) {
        this(component, handler.extractKeyType(component), handler.extractValueType(component), handler, nullReplacesNotNull);
    }

    @Override
    public void addField(final ToBeBuilt addFieldTo) {
        handler.addNonNullImmutableField(component, addFieldTo, keyType, valueType);
    }

    @Override
    public void writeGetter(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullImmutableGetter(component, methodBuilder, keyType, valueType);
    }

    @Override
    public void writeSetter(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullImmutableSetter(component, methodBuilder, keyType, valueType, nullReplacesNotNull);
    }

    @Override
    public void writeAddSingle(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullImmutableAddSingle(component, methodBuilder, keyType, valueType);
    }

    @Override
    public void writeRemoveSingle(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullImmutableRemoveSingle(component, methodBuilder, keyType);
    }

    @Override
    public void writeSpecialisedMethods(final ToBeBuilt toBeBuilt, final AruVisitor<?> visitor) {
        handler.writeNonNullImmutableSpecialisedMethods(component, visitor, toBeBuilt, keyType, valueType);
    }

    @Override
    public void writeMergerMethod(final MethodSpec.Builder methodBuilder) {
        handler.writeMergerMethod(keyType, valueType, methodBuilder);
    }

    @Override
    public void writeDifferMethod(final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord) {
        handler.writeDifferMethod(keyType, valueType, methodBuilder, collectionResultRecord);
    }

    @Override
    public void addDiffRecordComponents(final ToBeBuiltRecord recordBuilder) {
        handler.addDiffRecordComponents(keyType, valueType, recordBuilder);
    }
}

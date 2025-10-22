package io.github.cbarlin.aru.impl.types.collection.wrapper;

import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;

public record NonNullAutoCollectionHandler(
    AnalysedCollectionComponent component,
    CollectionHandler handler,
    boolean nullReplacesNotNull
) implements CollectionHandlerHelper {

    @Override
    public void addField(final ToBeBuilt addFieldTo) {
        handler.addNonNullAutoField(component, addFieldTo, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeGetter(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullAutoGetter(component, methodBuilder, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeSetter(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullAutoSetter(component, methodBuilder, component.unNestedPrimaryTypeName(), nullReplacesNotNull);
    }

    @Override
    public void writeAddSingle(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullAutoAddSingle(component, methodBuilder, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeRemoveSingle(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullAutoRemoveSingle(component, methodBuilder, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeRemovePredicate(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullAutoRemovePredicate(component, methodBuilder, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeRetainAll(final MethodSpec.Builder methodBuilder) {
        handler.writeNonNullAutoRetainAll(component, methodBuilder, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeAddMany(final BuilderClass builderClass, final String singleAdderName, final String addAllMethodName, final AruVisitor<?> visitor) {
        handler.writeNonNullAutoAddManyToBuilder(component, builderClass.delegate(), component.unNestedPrimaryTypeName(), singleAdderName, addAllMethodName, visitor);
    }

    @Override
    public void writeRemoveMany(final BuilderClass builderClass, final String singleAdderName, final String addAllMethodName, final AruVisitor<?> visitor) {
        handler.writeNonNullAutoRemoveManyToBuilder(component, builderClass.delegate(), component.unNestedPrimaryTypeName(), singleAdderName, addAllMethodName, visitor);
    }

    @Override
    public void writeMergerMethod(final MethodSpec.Builder methodBuilder) {
        handler.writeMergerMethod(component.unNestedPrimaryTypeName(), methodBuilder);
    }

    @Override
    public void writeDifferMethod(final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord) {
        handler.writeDifferMethod(component.unNestedPrimaryTypeName(), methodBuilder, collectionResultRecord);
    }

    @Override
    public void addDiffRecordComponents(final ToBeBuiltRecord recordBuilder) {
        handler.addDiffRecordComponents(component.unNestedPrimaryTypeName(), recordBuilder);
    }

}

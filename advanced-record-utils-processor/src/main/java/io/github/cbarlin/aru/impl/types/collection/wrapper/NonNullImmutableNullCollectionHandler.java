package io.github.cbarlin.aru.impl.types.collection.wrapper;

import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHelper;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec.Builder;

public record NonNullImmutableNullCollectionHandler(
    AnalysedCollectionComponent component,
    CollectionHandler handler,
    boolean nullReplacesNotNull
) implements CollectionHandlerHelper {

    @Override
    public void addField(ToBeBuilt addFieldTo) {
        handler.addNonNullImmutableField(component, addFieldTo, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeGetter(Builder methodBuilder) {
        handler.writeNonNullImmutableGetter(component, methodBuilder, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeSetter(Builder methodBuilder) {
        handler.writeNonNullImmutableSetter(component, methodBuilder, component.unNestedPrimaryTypeName(), nullReplacesNotNull);
    }

    @Override
    public void writeAddSingle(Builder methodBuilder) {
        handler.writeNonNullImmutableAddSingle(component, methodBuilder, component.unNestedPrimaryTypeName());
    }

    @Override
    public void writeAddMany(BuilderClass builderClass, String singleAdderName, String addAllMethodName, AruVisitor<?> visitor) {
        handler.writeNonNullImmutableAddManyToBuilder(component, builderClass.delegate(), component.unNestedPrimaryTypeName(), singleAdderName, addAllMethodName, visitor);
    }

    @Override
    public void writeMergerMethod(Builder methodBuilder) {
        handler.writeMergerMethod(component.unNestedPrimaryTypeName(), methodBuilder);
    }

    @Override
    public void writeDifferMethod(Builder methodBuilder, ClassName collectionResultRecord) {
        handler.writeDifferMethod(component.unNestedPrimaryTypeName(), methodBuilder, collectionResultRecord);
    }

    @Override
    public void addDiffRecordComponents(ToBeBuiltRecord recordBuilder) {
        handler.addDiffRecordComponents(component.unNestedPrimaryTypeName(), recordBuilder);
    }

}

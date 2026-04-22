package io.github.cbarlin.aru.impl.types.maps.eclipse;

import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec.Builder;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import javax.lang.model.element.Modifier;

public final class ImmutablePrimitiveMapHandler implements EclipseMapHandler {
    private static final String factoryPackage = "org.eclipse.collections.api.factory.primitive";
    private static final String containerPackage = "org.eclipse.collections.api.map.primitive";
    private static final String setPackage = "org.eclipse.collections.api.set.primitive";
    private final ClassName mapParent;
    private final ClassName immutable;
    private final ClassName mutable;
    private final ClassName factory;
    private final ClassName immutableKeySet;
    private final ClassName immutableKeySetFactory;
    private final TypeName key;
    private final TypeName value;

    public ImmutablePrimitiveMapHandler(
        final TypeName key,
        final TypeName value
    ) {
        final String keyName = EclipseMapHandler.strName(key);
        final String valueName = EclipseMapHandler.strName(value);
        this.key = key;
        this.value = value;
        factory = ClassName.get(factoryPackage, keyName + valueName + "Maps");
        mutable = ClassName.get(containerPackage, "Mutable" + keyName + valueName + "Map");
        immutable = ClassName.get(containerPackage, "Immutable" + keyName + valueName + "Map");
        mapParent = ClassName.get(containerPackage, keyName + valueName + "Map");
        immutableKeySet = ClassName.get(setPackage, "Immutable" + keyName + "Set");
        immutableKeySetFactory = ClassName.get(factoryPackage, keyName + "Sets");
    }

    @Override
    public boolean canHandle(final AnalysedComponent analysedComponent) {
        return immutable.toString().equals(analysedComponent.typeNameWithoutAnnotations().toString());
    }

    @Override
    public TypeName extractKeyType(final AnalysedComponent analysedComponent) {
        return key;
    }

    @Override
    public TypeName extractValueType(final AnalysedComponent analysedComponent) {
        return value;
    }

    @Override
    public void addNullableAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType) {
        final FieldSpec fieldSpec = FieldSpec.builder(
            mutable.annotated(CommonsConstants.NULLABLE_ANNOTATION),
            component.name(),
            Modifier.PRIVATE
        ).build();
        addFieldTo.addField(fieldSpec);
    }

    @Override
    public void writeNullableAutoGetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        methodBuilder.returns(immutable.annotated(CommonsConstants.NULLABLE_ANNOTATION))
                .beginControlFlow("if (this.$L != null)", component.name())
                .addStatement("return this.$L.toImmutable()", component.name())
                .endControlFlow()
                .addStatement("return null");
    }

    @Override
    public void writeNullableAutoSetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        final TypeName param = mapParent.annotated(CommonsConstants.NULLABLE_ANNOTATION);
        final String name = component.name();
        methodBuilder.addParameter(
            ParameterSpec.builder(param, name)
                .addJavadoc("Replacement value")
                .build()
        );
        if (nullReplacesNotNull) {
            methodBuilder.beginControlFlow("if ($L == null)", name)
                    .addStatement("this.$L = null", name)
                .nextControlFlow("else")
                    .addStatement("this.$L = $T.mutable.ofAll($L)", name, factory, name)
                .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($L != null)", name)
                .addStatement("this.$L = $T.mutable.ofAll($L)", name, factory, name)
                .endControlFlow();
        }
    }

    @Override
    public void writeNullableAutoAddSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        final String name = component.name();
        final ParameterSpec key = ParameterSpec.builder(keyType, "key")
                .addJavadoc("The key to add to the map")
                .build();
        final ParameterSpec value = ParameterSpec.builder(valueType, "value")
                .addJavadoc("The value to add to the map")
                .build();
        methodBuilder.addParameter(key)
                .addParameter(value)
                .beginControlFlow("if (this.$L == null)", name)
                .addStatement("this.$L = $T.mutable.empty()", name, factory)
                .endControlFlow()
                .addStatement("this.$L.put(key, value)", name);
    }

    @Override
    public void writeNullableAutoRemoveSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType) {
        final String name = component.name();
        final ParameterSpec key = ParameterSpec.builder(keyType, "key")
                .addJavadoc("The key to remove from the map")
                .build();
        methodBuilder.addParameter(key)
                .beginControlFlow("if (this.$L != null)", name)
                .addStatement("this.$L.remove(key)", name)
                .endControlFlow();
    }

    @Override
    public void addNullableImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType) {
        addNullableAutoField(component, addFieldTo, keyType, valueType);
    }

    @Override
    public void writeNullableImmutableGetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        writeNullableAutoGetter(component, methodBuilder, keyType, valueType);
    }

    @Override
    public void writeNullableImmutableSetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        writeNullableAutoSetter(component, methodBuilder, keyType, valueType, nullReplacesNotNull);
    }

    @Override
    public void writeNullableImmutableAddSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        writeNullableAutoAddSingle(component, methodBuilder, keyType, valueType);
    }

    @Override
    public void writeNullableImmutableRemoveSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType) {
        writeNullableAutoRemoveSingle(component, methodBuilder, keyType);
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType) {
        final FieldSpec fieldSpec = FieldSpec.builder(
                mutable.annotated(CommonsConstants.NON_NULL_ANNOTATION),
                component.name(),
                Modifier.PRIVATE,
                Modifier.FINAL
        )
                .initializer("$T.mutable.empty()", factory)
                .build();
        addFieldTo.addField(fieldSpec);
    }

    @Override
    public void writeNonNullAutoGetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        methodBuilder.returns(immutable.annotated(CommonsConstants.NON_NULL_ANNOTATION))
                .addStatement("return this.$L.toImmutable()", component.name());
    }

    @Override
    public void writeNonNullAutoSetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        final TypeName param = mapParent.annotated(CommonsConstants.NULLABLE_ANNOTATION);
        final String name = component.name();
        methodBuilder.addParameter(
                ParameterSpec.builder(param, name)
                        .addJavadoc("Replacement value")
                        .build()
        );
        if (nullReplacesNotNull) {
            methodBuilder.addStatement("this.$L.clear()", name)
                    .beginControlFlow("if ($L != null)", name)
                    .addStatement("this.$L.putAll($L)", name, name)
                    .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($L != null)", name)
                    .addStatement("this.$L.clear()", name)
                    .addStatement("this.$L.putAll($L)", name, name)
                    .endControlFlow();
        }
    }

    @Override
    public void writeNonNullAutoAddSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        final String name = component.name();
        final ParameterSpec key = ParameterSpec.builder(keyType, "key")
                .addJavadoc("The key to add to the map")
                .build();
        final ParameterSpec value = ParameterSpec.builder(valueType, "value")
                .addJavadoc("The value to add to the map")
                .build();
        methodBuilder.addParameter(key)
                .addParameter(value)
                .addStatement("this.$L.put(key, value)", name);
    }

    @Override
    public void writeNonNullAutoRemoveSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType) {
        final String name = component.name();
        final ParameterSpec key = ParameterSpec.builder(keyType, "key")
                .addJavadoc("The key to remove from the map")
                .build();
        methodBuilder.addParameter(key)
                .addStatement("this.$L.remove(key)", name);
    }

    @Override
    public void addNonNullImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType) {
        addNonNullAutoField(component, addFieldTo, keyType, valueType);
    }

    @Override
    public void writeNonNullImmutableGetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        writeNonNullAutoGetter(component, methodBuilder, keyType, valueType);
    }

    @Override
    public void writeNonNullImmutableSetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        writeNonNullAutoSetter(component, methodBuilder, keyType, valueType, nullReplacesNotNull);
    }

    @Override
    public void writeNonNullImmutableAddSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        writeNonNullAutoAddSingle(component, methodBuilder, keyType, valueType);
    }

    @Override
    public void writeNonNullImmutableRemoveSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType) {
        writeNonNullAutoRemoveSingle(component, methodBuilder, keyType);
    }

    @Override
    public void writeMergerMethod(final TypeName keyType, final TypeName valueType, final Builder methodBuilder) {
        final TypeName paramTypeName = immutable.annotated(CommonsConstants.NULLABLE_ANNOTATION);
        final ParameterSpec paramA = ParameterSpec.builder(paramTypeName, "elA", Modifier.FINAL)
                .addJavadoc("The preferred input")
                .build();

        final ParameterSpec paramB = ParameterSpec.builder(paramTypeName, "elB", Modifier.FINAL)
                .addJavadoc("The non-preferred input")
                .build();
        methodBuilder.addParameter(paramA)
                .addParameter(paramB)
                .returns(paramTypeName)
                .addJavadoc("Merger for fields of class {@link $T}", paramTypeName)
                .beginControlFlow("if (elA == null || elA.isEmpty())")
                .addStatement("return elB")
                .nextControlFlow("else if (elB == null || elB.isEmpty())")
                .addStatement("return elA")
                .endControlFlow()
                .addComment("It isn't documented, but the implementation here will create a new map preferring the arguments keys over the callers")
                .addComment("  so we must then call from our non-preferred")
                .addStatement("return $T.mutable.ofAll(elB).withAllKeyValues(elA.keyValuesView()).toImmutable()", factory);
    }

    @Override
    public void writeDifferMethod(final TypeName keyType, final TypeName valueType, final Builder methodBuilder, final ClassName collectionResultRecord) {
        final TypeName paramTypeName = immutable.annotated(CommonsConstants.NULLABLE_ANNOTATION);
        final TypeName setKey = immutableKeySet;
        methodBuilder.addParameter(ParameterSpec.builder(paramTypeName, "original", Modifier.FINAL).build())
                .addParameter(ParameterSpec.builder(paramTypeName, "updated", Modifier.FINAL).build())
                .returns(collectionResultRecord)
                .addStatement(
                        "final $T nUpdated = $T.requireNonNullElseGet(updated, () -> $T.immutable.empty())",
                        paramTypeName.withoutAnnotations(),
                        Constants.Names.OBJECTS,
                        factory
                )
                .addStatement(
                        "final $T nOriginal = $T.requireNonNullElseGet(original, () -> $T.immutable.empty())",
                        paramTypeName.withoutAnnotations(),
                        Constants.Names.OBJECTS,
                        factory
                )
                .addStatement(
                        "final $T addedKeys = $T.immutable.ofAll(nUpdated.keysView().reject(nOriginal::containsKey))",
                        setKey, immutableKeySetFactory
                )
                .addStatement(
                        "final $T removedKeys = $T.immutable.ofAll(nOriginal.keysView().reject(nUpdated::containsKey))",
                        setKey, immutableKeySetFactory
                )
                .addStatement(
                        "final $T commonKeys = $T.immutable.ofAll(nOriginal.keysView().select(nUpdated::containsKey))",
                        setKey, immutableKeySetFactory
                )
                .addStatement(
                        "final $T keysWithDifferentValues = commonKeys.reject(k -> $T.equals(nOriginal.get(k), nUpdated.get(k)))",
                        setKey, CommonsConstants.Names.OBJECTS
                )
                .addStatement(
                        "final $T keysWithSameValues = commonKeys.newWithoutAll(keysWithDifferentValues)",
                        setKey
                )
                .addStatement(
                        // Constructor is added, different, same, removed
                        "return new $T(addedKeys, keysWithDifferentValues, keysWithSameValues, removedKeys)",
                        collectionResultRecord
                );
    }

    @Override
    public void addDiffRecordComponents(final TypeName keyType, final TypeName valueType, final ToBeBuiltRecord recordBuilder) {
        final TypeName setKey = immutableKeySet.annotated(CommonsConstants.NON_NULL_ANNOTATION);
        recordBuilder.addParameterSpec(
                        ParameterSpec.builder(setKey, "addedKeys")
                                .addJavadoc("The keys that have been added")
                                .build()
                )
                .addParameterSpec(
                        ParameterSpec.builder(setKey, "keysWithDifferentValues")
                                .addJavadoc("The keys that exist in both maps, but that have different values (via {@link $T#equals($T, $T)})", Constants.Names.OBJECTS, Constants.Names.OBJECT, Constants.Names.OBJECT)
                                .build()
                )
                .addParameterSpec(
                        ParameterSpec.builder(setKey, "keysWithSameValues")
                                .addJavadoc("The keys that exist in both maps and that have the same values (via {@link $T#equals($T, $T)})", Constants.Names.OBJECTS, Constants.Names.OBJECT, Constants.Names.OBJECT)
                                .build()
                )
                .addParameterSpec(
                        ParameterSpec.builder(setKey, "removedKeys")
                                .addJavadoc("The keys that have been removed")
                                .build()
                );
    }
}

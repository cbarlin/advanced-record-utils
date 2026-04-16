package io.github.cbarlin.aru.impl.types.maps.eclipse;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec.Builder;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;
import javax.lang.model.element.Modifier;

public final class ImmutableNonPrimitiveMapHandler implements EclipseMapHandler {
    private static final ClassName iterable = DependencyClassNames.ECLIPSE_COLLECTIONS__MAP_ITERABLE;
    private static final ClassName immutable = DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_MAP;
    private static final ClassName mutable = DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_MAP;
    private static final ClassName factory = DependencyClassNames.ECLIPSE_COLLECTIONS__MAPS_FACTORY;

    @Override
    public boolean canHandle(final AnalysedComponent analysedComponent) {
        return analysedComponent.typeNameWithoutAnnotations() instanceof final ParameterizedTypeName ptn &&
            ptn.rawType.withoutAnnotations().equals(immutable) &&
            ptn.typeArguments.size() == 2;
    }

    @Override
    public TypeName extractKeyType(final AnalysedComponent analysedComponent) {
        return ((ParameterizedTypeName) analysedComponent.typeName()).typeArguments.getFirst().withoutAnnotations();
    }

    @Override
    public TypeName extractValueType(final AnalysedComponent analysedComponent) {
        return ((ParameterizedTypeName) analysedComponent.typeName()).typeArguments.getLast().withoutAnnotations();
    }

    @Override
    public void addNullableAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType) {
        final FieldSpec fieldSpec = FieldSpec.builder(
            ParameterizedTypeName.get(
                mutable,
                keyType,
                valueType
            ).annotated(CommonsConstants.NULLABLE_ANNOTATION),
            component.name(),
            Modifier.PRIVATE
        ).build();
        addFieldTo.addField(fieldSpec);
    }

    @Override
    public void writeNullableAutoGetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        methodBuilder.returns(ParameterizedTypeName.get(immutable, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION))
                .beginControlFlow("if (this.$L != null)", component.name())
                .addStatement("return this.$L.toImmutable()", component.name())
                .endControlFlow()
                .addStatement("return null");
    }

    @Override
    public void writeNullableAutoSetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        final TypeName param = ParameterizedTypeName.get(iterable, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION);
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
                    .addStatement("this.$L = $T.mutable.ofMapIterable($L)", name, factory, name)
                .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($L != null)", name)
                .addStatement("this.$L = $T.mutable.ofMapIterable($L)", name, factory, name)
                .endControlFlow();
        }
    }

    @Override
    public void writeNullableAutoAddSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        final String name = component.name();
        final ParameterSpec key = ParameterSpec.builder(keyType.annotated(CommonsConstants.NON_NULL_ANNOTATION), "key")
                .addJavadoc("The key to add to the map")
                .build();
        final ParameterSpec value = ParameterSpec.builder(valueType.annotated(CommonsConstants.NON_NULL_ANNOTATION), "value")
                .addJavadoc("The value to add to the map")
                .build();
        methodBuilder.addParameter(key)
                .addParameter(value)
                .addStatement("$T.requireNonNull(key, $S)", Constants.Names.OBJECTS, "Supplied key for addition cannot be null")
                .addStatement("$T.requireNonNull(value, $S)", Constants.Names.OBJECTS, "Supplied value to be added cannot be null")
                .beginControlFlow("if (this.$L == null)", name)
                .addStatement("this.$L = $T.mutable.empty()", name, factory)
                .endControlFlow()
                .addStatement("this.$L.put(key, value)", name);
    }

    @Override
    public void writeNullableAutoRemoveSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType) {
        final String name = component.name();
        final ParameterSpec key = ParameterSpec.builder(keyType.annotated(CommonsConstants.NON_NULL_ANNOTATION), "key")
                .addJavadoc("The key to remove from the map")
                .build();
        methodBuilder.addParameter(key)
                .addStatement("$T.requireNonNull(key, $S)", Constants.Names.OBJECTS, "Supplied key for removal cannot be null")
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
                ParameterizedTypeName.get(
                        mutable,
                        keyType,
                        valueType
                ).annotated(CommonsConstants.NON_NULL_ANNOTATION),
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
        methodBuilder.returns(ParameterizedTypeName.get(immutable, keyType, valueType).annotated(CommonsConstants.NON_NULL_ANNOTATION))
                .addStatement("return this.$L.toImmutable()", component.name());
    }

    @Override
    public void writeNonNullAutoSetter(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        final TypeName param = ParameterizedTypeName.get(iterable, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION);
        final String name = component.name();
        methodBuilder.addParameter(
                ParameterSpec.builder(param, name)
                        .addJavadoc("Replacement value")
                        .build()
        );
        if (nullReplacesNotNull) {
            methodBuilder.addStatement("this.$L.clear()", name)
                    .beginControlFlow("if ($L != null)", name)
                    .addStatement("this.$L.putAllMapIterable($L)", name, name)
                    .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($L != null)", name)
                    .addStatement("this.$L.clear()", name)
                    .addStatement("this.$L.putAllMapIterable($L)", name, name)
                    .endControlFlow();
        }
    }

    @Override
    public void writeNonNullAutoSpecialisedMethods(final AnalysedComponent component, final AruVisitor<?> visitor, final ToBeBuilt toBeBuilt, final TypeName keyType, final TypeName valueType) {
        writeSetMap(component, visitor, toBeBuilt, keyType, valueType);
    }

    private void writeSetMap(final AnalysedComponent component, final AruVisitor<?> visitor, final ToBeBuilt toBeBuilt, final TypeName keyType, final TypeName valueType) {
        final MethodSpec.Builder methodBuilder = toBeBuilt.createMethod(component.name(), CommonsConstants.Claims.CORE_BUILDER_SETTER, Constants.Names.MAP)
                .addAnnotation(CommonsConstants.Names.NON_NULL)
                .addModifiers(Modifier.PUBLIC);
        final String name = component.name();
        methodBuilder.returns(toBeBuilt.className())
                .addJavadoc("Updates the value of {@code $L}", name);
        final TypeName param = ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NON_NULL_ANNOTATION);
        methodBuilder.addParameter(
                ParameterSpec.builder(param, name)
                        .addJavadoc("Replacement value")
                        .build()
        );
        methodBuilder.beginControlFlow("if ($L != null)", name)
                .addStatement("this.$L.clear()", name)
                .addStatement("this.$L.putAll($L)", name, name)
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, visitor);
    }

    @Override
    public void writeNonNullImmutableSpecialisedMethods(final AnalysedComponent component, final AruVisitor<?> visitor, final ToBeBuilt toBeBuilt, final TypeName keyType, final TypeName valueType) {
        writeNonNullAutoSpecialisedMethods(component, visitor, toBeBuilt, keyType, valueType);
    }

    @Override
    public void writeNonNullAutoAddSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        final String name = component.name();
        final ParameterSpec key = ParameterSpec.builder(keyType.annotated(CommonsConstants.NON_NULL_ANNOTATION), "key")
                .addJavadoc("The key to add to the map")
                .build();
        final ParameterSpec value = ParameterSpec.builder(valueType.annotated(CommonsConstants.NON_NULL_ANNOTATION), "value")
                .addJavadoc("The value to add to the map")
                .build();
        methodBuilder.addParameter(key)
                .addParameter(value)
                .addStatement("$T.requireNonNull(key, $S)", Constants.Names.OBJECTS, "Supplied key for addition cannot be null")
                .addStatement("$T.requireNonNull(value, $S)", Constants.Names.OBJECTS, "Supplied value to be added cannot be null")
                .addStatement("this.$L.put(key, value)", name);
    }

    @Override
    public void writeNonNullAutoRemoveSingle(final AnalysedComponent component, final Builder methodBuilder, final TypeName keyType) {
        final String name = component.name();
        final ParameterSpec key = ParameterSpec.builder(keyType.annotated(CommonsConstants.NON_NULL_ANNOTATION), "key")
                .addJavadoc("The key to remove from the map")
                .build();
        methodBuilder.addParameter(key)
                .addStatement("$T.requireNonNull(key, $S)", Constants.Names.OBJECTS, "Supplied key for removal cannot be null")
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
        final TypeName paramTypeName = ParameterizedTypeName.get(immutable, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION);
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
                .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", Constants.Names.OBJECTS)
                .addStatement("return elB")
                .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", Constants.Names.OBJECTS)
                .addStatement("return elA")
                .endControlFlow()
                .addComment("It isn't documented, but the implementation here will create a new map preferring the arguments keys over the callers")
                .addComment("  so we must then call from our non-preferred")
                .addStatement("return elB.newWithMapIterable(elA)");
    }

    @Override
    public void writeDifferMethod(final TypeName keyType, final TypeName valueType, final Builder methodBuilder, final ClassName collectionResultRecord) {
        final TypeName paramTypeName = ParameterizedTypeName.get(immutable, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION);
        final TypeName setKey = ParameterizedTypeName.get(DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_SET, keyType);
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
                        "final $T addedKeys = nUpdated.keysView().reject(nOriginal::containsKey).toImmutableSet()",
                        setKey
                )
                .addStatement(
                        "final $T removedKeys = nOriginal.keysView().reject(nUpdated::containsKey).toImmutableSet()",
                        setKey
                )
                .addStatement(
                        "final $T commonKeys = nOriginal.keysView().select(nUpdated::containsKey).toImmutableSet()",
                        setKey
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
        final TypeName setKey = ParameterizedTypeName.get(DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_SET, keyType).annotated(CommonsConstants.NON_NULL_ANNOTATION);
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

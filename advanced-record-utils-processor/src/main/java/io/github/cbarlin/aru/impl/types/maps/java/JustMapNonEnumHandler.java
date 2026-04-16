package io.github.cbarlin.aru.impl.types.maps.java;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.maps.MapHandler;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;
import javax.lang.model.element.Modifier;

@Component
@GlobalScope
public class JustMapNonEnumHandler implements MapHandler {

    @Override
    public boolean canHandle(final AnalysedComponent analysedComponent) {
        return analysedComponent.typeNameWithoutAnnotations() instanceof final ParameterizedTypeName ptn &&
            ptn.rawType.withoutAnnotations().equals(Constants.Names.MAP) &&
            ptn.typeArguments.size() == 2 &&
            !OptionalClassDetector.checkSameOrSubType(
                    ptn.typeArguments.getFirst(),
                    Constants.Names.ENUM
            );
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
                ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION),
                component.name(),
                Modifier.PRIVATE
        ).build();
        addFieldTo.addField(fieldSpec);
    }

    @Override
    public void writeNullableAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        methodBuilder.addStatement("return this.$L", component.name())
                .returns(ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION));
    }

    @Override
    public void writeNullableAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        final String name = component.name();
        final ParameterSpec param = ParameterSpec.builder(
                        ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION),
                        name
                )
                .addJavadoc("Replacement value")
                .build();
        methodBuilder.addParameter(param);
        if (nullReplacesNotNull) {
            methodBuilder.beginControlFlow("if ($L == null)", name)
                    .addStatement("this.$L = null", name)
                    .nextControlFlow("else")
                    .addStatement("this.$L = new $T<>($L)", name, Constants.Names.HASH_MAP, name)
                    .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($L != null)", name)
                    .addStatement("this.$L = new $T<>($L)", name, Constants.Names.HASH_MAP, name)
                    .endControlFlow();
        }
    }

    @Override
    public void writeNullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
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
                .addStatement("this.$L = new $T<>()", name, Constants.Names.HASH_MAP)
                .endControlFlow()
                .addStatement("this.$L.put(key, value)", name);
    }

    @Override
    public void writeNullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType) {
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
    public void writeNullableImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        final String name = component.name();
        methodBuilder.returns(ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION))
                .beginControlFlow("if (this.$L != null)", name)
                .addStatement("return $T.copyOf(this.$L)", Constants.Names.MAP, name)
                .endControlFlow()
                .addStatement("return null");
    }

    @Override
    public void writeNullableImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        writeNullableAutoSetter(component, methodBuilder, keyType, valueType, nullReplacesNotNull);
    }

    @Override
    public void writeNullableImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        writeNullableAutoAddSingle(component, methodBuilder, keyType, valueType);
    }

    @Override
    public void writeNullableImmutableRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType) {
        writeNullableAutoRemoveSingle(component, methodBuilder, keyType);
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType) {
        final FieldSpec fieldSpec = FieldSpec.builder(
                    ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NON_NULL_ANNOTATION),
                    component.name(),
                    Modifier.PRIVATE,
                    Modifier.FINAL
                )
                .initializer("new $T<>()", Constants.Names.HASH_MAP)
                .build();
        addFieldTo.addField(fieldSpec);
    }

    @Override
    public void writeNonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        methodBuilder.addStatement("return this.$L", component.name())
                .returns(ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NON_NULL_ANNOTATION));
    }

    @Override
    public void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        final String name = component.name();
        final ParameterSpec param = ParameterSpec.builder(
                        ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION),
                        name
                )
                .addJavadoc("Replacement value")
                .build();
        methodBuilder.addParameter(param);
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
    public void writeNonNullAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
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
    public void writeNonNullAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType) {
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
    public void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        final String name = component.name();
        methodBuilder.returns(ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NON_NULL_ANNOTATION))
                .addStatement("return $T.copyOf(this.$L)", Constants.Names.MAP, name);
    }

    @Override
    public void writeNonNullImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        writeNonNullAutoSetter(component, methodBuilder, keyType, valueType, nullReplacesNotNull);
    }

    @Override
    public void writeNonNullImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        writeNonNullAutoAddSingle(component, methodBuilder, keyType, valueType);
    }

    @Override
    public void writeNonNullImmutableRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType) {
        writeNonNullAutoRemoveSingle(component, methodBuilder, keyType);
    }

    @Override
    public void writeMergerMethod(final TypeName keyType, final TypeName valueType, final MethodSpec.Builder methodBuilder) {
        final TypeName paramTypeName = ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION);
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
                .addStatement(
                    "final $T<$T, $T> combined = new $T<>()",
                    Constants.Names.MAP,
                    keyType,
                    valueType,
                    Constants.Names.HASH_MAP
                )
                // putAll overrides existing entries, so put all the non-preferred ones first
                .addStatement("combined.putAll(elB)")
                .addStatement("combined.putAll(elA)")
                // Since this goes via the builder, that will handle things like immutability
                .addStatement("return combined");
    }

    @Override
    public void writeDifferMethod(final TypeName keyType, final TypeName valueType, final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord) {
        final TypeName paramTypeName = ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION);
        final TypeName setKey = ParameterizedTypeName.get(Constants.Names.SET, keyType).annotated(CommonsConstants.NON_NULL_ANNOTATION);
        methodBuilder.addParameter(ParameterSpec.builder(paramTypeName, "original", Modifier.FINAL).build())
                .addParameter(ParameterSpec.builder(paramTypeName, "updated", Modifier.FINAL).build())
                .returns(collectionResultRecord)
                .addStatement(
                        "final $T nUpdated = $T.requireNonNullElseGet(updated, $T::emptyMap)",
                        paramTypeName,
                        Constants.Names.OBJECTS,
                        Constants.Names.COLLECTIONS
                )
                .addStatement(
                        "final $T nOriginal = $T.requireNonNullElseGet(original, $T::emptyMap)",
                        paramTypeName,
                        Constants.Names.OBJECTS,
                        Constants.Names.COLLECTIONS
                )
                .addStatement(
                        "final $T addedKeys = nUpdated.keySet().stream().filter($T.not(nOriginal::containsKey)).collect($T.toUnmodifiableSet())",
                        setKey,
                        Constants.Names.PREDICATE,
                        CommonsConstants.Names.COLLECTORS
                )
                .addStatement(
                        "final $T removedKeys = nOriginal.keySet().stream().filter($T.not(nUpdated::containsKey)).collect($T.toUnmodifiableSet())",
                        setKey,
                        Constants.Names.PREDICATE,
                        CommonsConstants.Names.COLLECTORS
                )
                .addStatement(
                        "final $T commonKeys = nOriginal.keySet().stream().filter(nUpdated::containsKey).collect($T.toUnmodifiableSet())",
                        setKey,
                        CommonsConstants.Names.COLLECTORS
                )
                .addStatement(
                        "final $T keysWithDifferentValues = $T.newHashSet(commonKeys.size())",
                        setKey,
                        CommonsConstants.Names.HASH_SET
                )
                .addStatement(
                        "final $T keysWithSameValues = $T.newHashSet(commonKeys.size())",
                        setKey,
                        CommonsConstants.Names.HASH_SET
                )
                .beginControlFlow("for (final $T key : commonKeys)", keyType)
                .beginControlFlow("if ($T.equals(nOriginal.get(key), nUpdated.get(key)))", Constants.Names.OBJECTS)
                .addStatement("keysWithSameValues.add(key)")
                .nextControlFlow("else")
                .addStatement("keysWithDifferentValues.add(key)")
                .endControlFlow()
                .endControlFlow()
                .addStatement(
                        // Constructor is added, different, same, removed
                        "return new $T(addedKeys, $T.unmodifiableSet(keysWithDifferentValues), $T.unmodifiableSet(keysWithSameValues), removedKeys)",
                        collectionResultRecord,
                        Constants.Names.COLLECTIONS,
                        Constants.Names.COLLECTIONS
                );
    }

    @Override
    public void addDiffRecordComponents(final TypeName keyType, final TypeName valueType, final ToBeBuiltRecord recordBuilder) {
        final TypeName setKey = ParameterizedTypeName.get(Constants.Names.SET, keyType).annotated(CommonsConstants.NON_NULL_ANNOTATION);
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

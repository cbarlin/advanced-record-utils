package io.github.cbarlin.aru.impl.types.maps.java;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Component
@GlobalScope
public final class JustMapEnumHandler extends JustMapNonEnumHandler {

    @Override
    public boolean canHandle(final AnalysedComponent analysedComponent) {
        return analysedComponent.typeNameWithoutAnnotations() instanceof final ParameterizedTypeName ptn &&
            ptn.rawType.withoutAnnotations().equals(Constants.Names.MAP) &&
            ptn.typeArguments.size() == 2 &&
            OptionalClassDetector.checkSameOrSubType(
                    ptn.typeArguments.getFirst(),
                    Constants.Names.ENUM
            );
    }

    @Override
    public void writeNullableImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        final String name = component.name();
        methodBuilder.returns(ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION))
                .beginControlFlow("if (this.$L != null)", name)
                .addStatement("return $T.unmodifiableMap(new $T<>(this.$L))", Constants.Names.COLLECTIONS, Constants.Names.ENUM_MAP, name)
                .endControlFlow()
                .addStatement("return null");
    }

    @Override
    public void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
        final String name = component.name();
        methodBuilder.returns(ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NON_NULL_ANNOTATION))
                .addStatement("return $T.unmodifiableMap(new $T<>(this.$L))", Constants.Names.COLLECTIONS, Constants.Names.ENUM_MAP, name);
    }

    @Override
    public void addNullableImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType) {
        final FieldSpec fieldSpec = FieldSpec.builder(
                ParameterizedTypeName.get(Constants.Names.ENUM_MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION),
                component.name(),
                Modifier.PRIVATE
        ).build();
        addFieldTo.addField(fieldSpec);
    }

    @Override
    public void writeNullableImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType, final boolean nullReplacesNotNull) {
        final String name = component.name();
        final ParameterSpec param = ParameterSpec.builder(
                        ParameterizedTypeName.get(Constants.Names.MAP, keyType, valueType).annotated(CommonsConstants.NULLABLE_ANNOTATION),
                        name
                )
                .addJavadoc("Replacement value")
                .build();
        methodBuilder.addParameter(param);
        methodBuilder.beginControlFlow("if (this.$L == null)", name)
                .addStatement("this.$L = new $T<>($T.class)", name, Constants.Names.ENUM_MAP, keyType)
                .endControlFlow();
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
    public void writeNullableImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName keyType, final TypeName valueType) {
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
                .addStatement("this.$L = new $T<>($T.class)", name, Constants.Names.ENUM_MAP, keyType)
                .endControlFlow()
                .addStatement("this.$L.put(key, value)", name);
    }

    @Override
    public void addNonNullImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName keyType, final TypeName valueType) {
        final FieldSpec fieldSpec = FieldSpec.builder(
                ParameterizedTypeName.get(Constants.Names.ENUM_MAP, keyType, valueType).annotated(CommonsConstants.NON_NULL_ANNOTATION),
                component.name(),
                Modifier.PRIVATE
        )
                .initializer("new $T<>($T.class)", Constants.Names.ENUM_MAP, keyType)
                .build();
        addFieldTo.addField(fieldSpec);
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
                .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", OBJECTS)
                .addStatement("return elB")
                .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", OBJECTS)
                .addStatement("return elA")
                .endControlFlow()
                .addStatement(
                    "final $T<$T, $T> combined = new $T<>($T.class)",
                    Constants.Names.MAP,
                    keyType,
                    valueType,
                    Constants.Names.ENUM_MAP,
                    keyType
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
                        "final $T addedKeys = $T.noneOf($T.class)",
                        setKey,
                        Constants.Names.ENUM_SET,
                        keyType
                )
                .addStatement(
                        "final $T removedKeys = $T.noneOf($T.class)",
                        setKey,
                        Constants.Names.ENUM_SET,
                        keyType
                )
                .addStatement(
                        "final $T commonKeys = $T.noneOf($T.class)",
                        setKey,
                        Constants.Names.ENUM_SET,
                        keyType
                )
                .addStatement(
                        "final $T keysWithDifferentValues = $T.noneOf($T.class)",
                        setKey,
                        Constants.Names.ENUM_SET,
                        keyType
                )
                .addStatement(
                        "final $T keysWithSameValues = $T.noneOf($T.class)",
                        setKey,
                        Constants.Names.ENUM_SET,
                        keyType
                )
                .addStatement(
                        "nUpdated.keySet().stream().filter($T.not(nOriginal::containsKey)).forEach(addedKeys::add)",
                        Constants.Names.PREDICATE
                )
                .addStatement(
                        "nOriginal.keySet().stream().filter($T.not(nUpdated::containsKey)).forEach(removedKeys::add)",
                        Constants.Names.PREDICATE
                )
                .addStatement(
                        "nOriginal.keySet().stream().filter(nUpdated::containsKey).forEach(commonKeys::add)"
                )
                .beginControlFlow("for (final $T key : commonKeys)", keyType)
                .beginControlFlow("if ($T.equals(nOriginal.get(key), nUpdated.get(key)))", Constants.Names.OBJECTS)
                .addStatement("keysWithSameValues.add(key)")
                .nextControlFlow("else")
                .addStatement("keysWithDifferentValues.add(key)")
                .endControlFlow()
                .endControlFlow()
                .addStatement(
                        // Constructor is added, removed, different, same
                        "return new $T($T.unmodifiableSet(addedKeys), $T.unmodifiableSet(removedKeys), $T.unmodifiableSet(keysWithDifferentValues), $T.unmodifiableSet(keysWithSameValues))",
                        collectionResultRecord,
                        Constants.Names.COLLECTIONS,
                        Constants.Names.COLLECTIONS,
                        Constants.Names.COLLECTIONS,
                        Constants.Names.COLLECTIONS
                );
    }
}

package io.github.cbarlin.aru.impl.types.collection.set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;
import static io.github.cbarlin.aru.impl.Constants.Names.ENUM_SET;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@GlobalScope
@SuppressWarnings({"java:S1192"}) // Constant strings will not make this class clearer
public final class EnumSetCollectionHandler extends SetCollectionHandler {

    public EnumSetCollectionHandler() {
        super(ENUM_SET, ENUM_SET);
    }

    public static void convertImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = $T.copyOf($L)", SET, innerTypeName, assignmentName, SET, fieldName);
    }

    public static void nullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.addStatement("$T.requireNonNull($L, $S)", OBJECTS, component.name(), "Cannot add a null item to a set of enums")
            .beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("this.$L = $T.noneOf($T.class)", component.name(), ENUM_SET, innerType)
            .nextControlFlow("else if (!(this.$L instanceof $T))", component.name(), ENUM_SET)
            .addStatement("this.$L = $T.copyOf(this.$L)", component.name(), ENUM_SET, component.name())
            .endControlFlow()
            .addStatement("this.$L.add($L)", component.name(), component.name());
    }

    public static void nullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.addStatement("$T.requireNonNull($L, $S)", OBJECTS, component.name(), "Cannot add a null item to a set of enums")
            .beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("this.$L = $T.noneOf($T.class)", component.name(), ENUM_SET, innerType)
            .nextControlFlow("else if (!(this.$L instanceof $T))", component.name(), ENUM_SET)
            .addStatement("this.$L = $T.copyOf(this.$L)", component.name(), ENUM_SET, component.name())
            .endControlFlow()
            .addStatement("this.$L.remove($L)", component.name(), component.name());
    }

    public static void nullableAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.addStatement("$T.requireNonNull($L, $S)", OBJECTS, component.name(), "Cannot add a null item to a set of enums")
            .beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("this.$L = $T.noneOf($T.class)", component.name(), ENUM_SET, innerType)
            .nextControlFlow("else if (!(this.$L instanceof $T))", component.name(), ENUM_SET)
            .addStatement("this.$L = $T.copyOf(this.$L)", component.name(), ENUM_SET, component.name())
            .endControlFlow()
            .addStatement("this.$L.removeIf($L)", component.name(), component.name());
    }

    public static void nullableAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.addStatement("$T.requireNonNull($L, $S)", OBJECTS, component.name(), "Cannot add a null item to a set of enums")
            .beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("this.$L = $T.noneOf($T.class)", component.name(), ENUM_SET, innerType)
            .nextControlFlow("else if (!(this.$L instanceof $T))", component.name(), ENUM_SET)
            .addStatement("this.$L = $T.copyOf(this.$L)", component.name(), ENUM_SET, component.name())
            .endControlFlow()
            .addStatement("this.$L.retainAll($L)", component.name(), component.name());
    }

    public static void mergerMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder, final ClassName classNameOnComponent) {
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(classNameOnComponent, innerType);
        final ParameterSpec paramA = ParameterSpec.builder(paramTypeName, "elA", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The preferred input")
            .build();
        
        final ParameterSpec paramB = ParameterSpec.builder(paramTypeName, "elB", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The non-preferred input")
            .build();
        methodBuilder.addAnnotation(NULLABLE)
            .addParameter(paramA)
            .addParameter(paramB)
            .returns(paramTypeName)
            .addJavadoc("Merger for fields of class {@link $T}", paramTypeName)
            .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", OBJECTS)
            .addStatement("return elB")
            .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", OBJECTS)
            .addStatement("return elA")
            .endControlFlow()
            .addStatement("final $T<$T> combined = $T.noneOf($T.class)", ENUM_SET, innerType, ENUM_SET, innerType)
            .addStatement("combined.addAll(elA)")
            .addStatement("combined.addAll(elB)")
            // Since this goes via the builder, that will handle things like immutability
            .addStatement("return combined");
    }

    public static void nonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final ParameterizedTypeName ptn = ParameterizedTypeName.get(ENUM_SET, innerType);
        final FieldSpec fSpec = FieldSpec.builder(ptn, component.name(), Modifier.PRIVATE)
            .addAnnotation(NON_NULL)
            .initializer("$T.noneOf($T.class)", ENUM_SET, innerType)
            .build();
        addFieldTo.addField(fSpec);
    }

    public static void nonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("return $T.noneOf($T.class)", ENUM_SET, innerType)
            .endControlFlow()
            .addStatement("return this.$L", component.name())
            .returns(component.typeName());
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        EnumSetCollectionHandler.convertImmutable(methodBuilder, fieldName, assignmentName, innerTypeName);
    }

    @Override
    public void writeNonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        EnumSetCollectionHandler.nonNullAutoGetter(component, methodBuilder, innerType);
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        EnumSetCollectionHandler.nonNullAutoField(component, addFieldTo, innerType);
    }

    @Override
    public void writeNullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        EnumSetCollectionHandler.nullableAutoAddSingle(component, methodBuilder, innerType);
    }

    @Override
    public void writeNullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        EnumSetCollectionHandler.nullableAutoRemoveSingle(component, methodBuilder, innerType);
    }

    @Override
    public void writeNullableAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        EnumSetCollectionHandler.nullableAutoRemovePredicate(component, methodBuilder, innerType);
    }

    @Override
    public void writeNullableAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        EnumSetCollectionHandler.nullableAutoRetainAll(component, methodBuilder, innerType);
    }

    @Override
    public void writeMergerMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder) {
        EnumSetCollectionHandler.mergerMethod(innerType, methodBuilder, classNameOnComponent);
    }

    @Override
    public void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.addStatement("return this.$L", component.name())
            .returns(component.typeName());
    }
}

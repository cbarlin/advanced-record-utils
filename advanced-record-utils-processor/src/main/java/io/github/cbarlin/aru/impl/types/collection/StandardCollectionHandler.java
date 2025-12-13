package io.github.cbarlin.aru.impl.types.collection;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@SuppressWarnings({"java:S1192"}) // Adding a constant will not make the code clearer
public abstract class StandardCollectionHandler extends CollectionHandler {

    protected final ClassName classNameOnComponent;
    protected final ClassName mutableClassName;
    protected final ClassName immutableClassName;

    protected StandardCollectionHandler(final ClassName classNameOnComponent, final ClassName mutableClassName, final ClassName immutableClassName) {
        this.classNameOnComponent = classNameOnComponent;
        this.mutableClassName = mutableClassName;
        this.immutableClassName = immutableClassName;
    }

    /**
     * Add the code to the builder that converts the current item to an immutable form
     */
    protected abstract void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String targetVariableName, final TypeName innerTypeName);

    @Override
    public boolean canHandle(final AnalysedComponent component) {
        return component.typeName() instanceof final ParameterizedTypeName ptn && ptn.rawType.equals(classNameOnComponent);
    }

    @Override
    public void addNullableAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final FieldSpec fSpec = FieldSpec.builder(component.typeNameNullable(), component.name(), Modifier.PRIVATE)
            .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    public void writeNullableAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        if (nullReplacesNotNull) {
            methodBuilder.addStatement("this.$L = $L", component.name(), component.name());
        } else {
            methodBuilder.addStatement("this.$L = $T.nonNull($L) ? $L : this.$L", component.name(), OBJECTS, component.name(), component.name(), component.name());
        }
        
    }

    @Override
    public void writeNullableImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        writeNullableAutoSetter(component, methodBuilder, innerType, nullReplacesNotNull);
    }

    @Override
    public void addNullableImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        addNullableAutoField(component, addFieldTo, innerType);
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final ParameterizedTypeName ptn = ParameterizedTypeName.get(mutableClassName, innerType);
        final FieldSpec fSpec = FieldSpec.builder(ptn, component.name(), Modifier.PRIVATE)
            .initializer("new $T<$T>()", mutableClassName, innerType)
            .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    public void addNonNullImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        addNonNullAutoField(component, addFieldTo, innerType);
    }

    @Override
    public void writeNullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        if (!mutableClassName.equals(classNameOnComponent)) {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                 .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
                 .nextControlFlow("else if (!(this.$L instanceof $T))", component.name(), mutableClassName)
                 .addStatement("this.$L = new $T<$T>(this.$L)", component.name(), mutableClassName, innerType, component.name())
                 .endControlFlow()
                 .addStatement("this.$L.add($L)", component.name(), component.name());
        } else {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                 .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
                 .endControlFlow()
                 .addStatement("this.$L.add($L)", component.name(), component.name());
        }
    }

    @Override
    public void writeNullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        if (!mutableClassName.equals(classNameOnComponent)) {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                         .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
                         .nextControlFlow("else if (!(this.$L instanceof $T))", component.name(), mutableClassName)
                         .addStatement("this.$L = new $T<$T>(this.$L)", component.name(), mutableClassName, innerType, component.name())
                         .endControlFlow()
                         .addStatement("this.$L.remove($L)", component.name(), component.name());
        } else {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                         .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
                         .endControlFlow()
                         .addStatement("this.$L.remove($L)", component.name(), component.name());
        }
    }

    @Override
    public void writeNullableAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        if (!mutableClassName.equals(classNameOnComponent)) {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                         .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
                         .nextControlFlow("else if (!(this.$L instanceof $T))", component.name(), mutableClassName)
                         .addStatement("this.$L = new $T<$T>(this.$L)", component.name(), mutableClassName, innerType, component.name())
                         .endControlFlow()
                         .addStatement("this.$L.removeIf($L)", component.name(), component.name());
        } else {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                         .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
                         .endControlFlow()
                         .addStatement("this.$L.removeIf($L)", component.name(), component.name());
        }
    }

    @Override
    public void writeNullableAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        if (!mutableClassName.equals(classNameOnComponent)) {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                 .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
                 .nextControlFlow("else if (!(this.$L instanceof $T))", component.name(), mutableClassName)
                 .addStatement("this.$L = new $T<$T>(this.$L)", component.name(), mutableClassName, innerType, component.name())
                 .endControlFlow()
                 .addStatement("this.$L.retainAll($L)", component.name(), component.name());
        } else {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                 .addStatement("this.$L = new $T<$T>()", component.name(), mutableClassName, innerType)
                 .endControlFlow()
                 .addStatement("this.$L.retainAll($L)", component.name(), component.name());
        }
    }

    @Override
    public void writeNullableImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        if (classNameOnComponent.equals(immutableClassName)) {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                .addStatement("return null")
                .endControlFlow();
            convertToImmutable(methodBuilder, "this." + component.name(), "___immutable", innerType);
            methodBuilder.addStatement("return ___immutable")
                .returns(component.typeNameNullable())
                .addJavadoc("Returns the current value of {@code $L}", component.name());
        } else {
            writeNullableAutoGetter(component, methodBuilder, innerType);
        }
    }

    @Override
    public void writeNullableImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNullableAutoAddSingle(component, methodBuilder, innerType);
    }

    @Override
    public void writeNullableImmutableRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNullableAutoRemoveSingle(component, methodBuilder, innerType);
    }

    @Override
    public void writeNullableImmutableRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNullableAutoRemovePredicate(component, methodBuilder, innerType);
    }

    @Override
    public void writeNullableImmutableRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNullableAutoRetainAll(component, methodBuilder, innerType);
    }

    @Override
    public void writeNonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        // The field in the builder is also NonNull in this case, so no need for null check
        methodBuilder.addStatement("return this.$L", component.name())
            .returns(component.typeName());
    }

    @Override
    public void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        if (nullReplacesNotNull) {
            methodBuilder.addStatement("this.$L.clear()", component.name())
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, component.name())
                .addStatement("this.$L.addAll($L)", component.name(), component.name())
                .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull($L))", OBJECTS, component.name())
                .addStatement("this.$L.clear()", component.name())
                .addStatement("this.$L.addAll($L)", component.name(), component.name())
                .endControlFlow();
        }
        
    }

    @Override
    public void writeNonNullAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String name = component.name();
        methodBuilder.addStatement("this.$L.add($L)", name, name);
    }

    @Override
    public void writeNonNullAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String name = component.name();
        methodBuilder.addStatement("this.$L.remove($L)", name, name);
    }

    @Override
    public void writeNonNullAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String name = component.name();
        methodBuilder.addStatement("this.$L.removeIf($L)", name, name);
    }

    @Override
    public void writeNonNullAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String name = component.name();
        methodBuilder.addStatement("this.$L.retainAll($L)", name, name);
    }

    @Override
    public void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        // The field in the builder is also NonNull in this case, so no need for null check
        convertToImmutable(methodBuilder, "this." + component.name(), "___immutable", innerType);
        methodBuilder.addStatement("return ___immutable")
            .returns(component.typeName())
            .addJavadoc("Returns the current value of {@code $L}", component.name());
    }

    @Override
    public void writeNonNullImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        writeNonNullAutoSetter(component, methodBuilder, innerType, nullReplacesNotNull);
    }

    @Override
    public void writeNonNullImmutableAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNonNullAutoAddSingle(component, methodBuilder, innerType);
    }

    @Override
    public void writeNonNullImmutableRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNonNullAutoRemoveSingle(component, methodBuilder, innerType);
    }

    @Override
    public void writeNonNullImmutableRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNonNullAutoRemovePredicate(component, methodBuilder, innerType);
    }

    @Override
    public void writeNonNullImmutableRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNonNullAutoRetainAll(component, methodBuilder, innerType);
    }

    @Override
    public void writeMergerMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder) {
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
            .addStatement("final $T<$T> combined = new $T()", mutableClassName, innerType, mutableClassName)
            .addStatement("combined.addAll(elA)")
            .addStatement("combined.addAll(elB)")
            // Since this goes via the builder, that will handle things like immutability
            .addStatement("return combined");
    }

    @Override
    public void addDiffRecordComponents(final TypeName innerType, final ToBeBuiltRecord recordBuilder) {
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(immutableClassName, innerType);
        recordBuilder.addParameterSpec(
                ParameterSpec.builder(paramTypeName, "addedElements")
                    .addJavadoc("The elements added to the collection")
                    .build()  
            )
            .addParameterSpec(
                ParameterSpec.builder(paramTypeName, "removedElements")
                    .addJavadoc("The elements removed from the collection")
                    .build()  
            )
            .addParameterSpec(
                ParameterSpec.builder(paramTypeName, "elementsInCommon")
                    .addJavadoc("The elements in common between the two instances")
                    .build()  
            );
    }
}

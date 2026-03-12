package io.github.cbarlin.aru.impl.types.collection.hppc;

import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.List;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

public abstract class AbstractHppcPrimitiveHandler extends CollectionHandler {

    protected final List<ClassName> possibleClassNames;
    protected final ClassName concreteClassName;

    protected AbstractHppcPrimitiveHandler(
        final List<ClassName> possibleClassNames,
        final ClassName concreteClassName
    ) {
        this.possibleClassNames = possibleClassNames;
        this.concreteClassName = concreteClassName;
    }

    @Override
    public final boolean canHandle(final AnalysedComponent component) {
        return component.className()
                .filter(possibleClassNames::contains)
                .isPresent();
    }

    @Override
    public final void addNullableAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final FieldSpec fSpec = FieldSpec.builder(component.typeNameNullable(), component.name(), Modifier.PRIVATE)
                .initializer("null")
                .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    public final void writeNullableAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        if (nullReplacesNotNull) {
            methodBuilder.addStatement("this.$L = $L", component.name(), component.name());
        } else {
            methodBuilder.addStatement("this.$L = $T.nonNull($L) ? $L : this.$L", component.name(), OBJECTS, component.name(), component.name(), component.name());
        }
    }

    @Override
    public final void writeNullableImmutableSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        writeNullableAutoSetter(component, methodBuilder, innerType, nullReplacesNotNull);
    }

    @Override
    public final void addNullableImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        addNullableAutoField(component, addFieldTo, innerType);
    }

    @Override
    public final void addNonNullAutoField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final FieldSpec fSpec = FieldSpec.builder(component.typeNameNonNull(), component.name(), Modifier.PRIVATE)
                .initializer("new $T()", concreteClassName)
                .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    public final void addNonNullImmutableField(final AnalysedComponent component, final ToBeBuilt addFieldTo, final TypeName innerType) {
        addNonNullAutoField(component, addFieldTo, innerType);
    }

    @Override
    public void writeNullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
                .addStatement("this.$L = new $T()", component.name(), concreteClassName)
                .endControlFlow()
                .addStatement("this.$L.add($L)", component.name(), component.name());
    }

    @Override
    public void writeNullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, component.name())
            .addStatement("this.$L = new $T()", component.name(), concreteClassName)
            .endControlFlow()
            .addStatement("this.$L.removeElement($L)", component.name(), component.name());
    }

    @Override
    public void writeNullableAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, component.name())
                .addStatement("this.$L.removeAll($L)", component.name(), component.name())
                .endControlFlow();
    }

    @Override
    public void writeNullableAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, component.name())
                .addStatement("this.$L.retainAll(__e -> $L.contains(__e))", component.name(), component.name())
                .endControlFlow();
    }

    @Override
    public void writeNullableAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, component.name())
                .addStatement("return new $T(this.$L)", concreteClassName, component.name())
                .nextControlFlow("else")
                .addStatement("return null")
                .endControlFlow()
                .returns(component.typeName())
                .addJavadoc("Returns the current value of {@code $L}", component.name());
    }

    @Override
    public void writeNullableImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        writeNullableAutoGetter(component, methodBuilder, innerType);
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
        methodBuilder.addStatement("this.$L.removeElement($L)", name, name);
    }

    @Override
    public void writeNonNullAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String name = component.name();
        methodBuilder.addStatement("this.$L.removeIf($L)", name, name);
    }

    @Override
    public void writeNonNullAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String name = component.name();
        methodBuilder.addStatement("this.$L.retainAll(__e -> $L.contains(__e))", name, name);
    }

    @Override
    public void writeNonNullImmutableGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        methodBuilder.addStatement("return new $T(this.$L)", concreteClassName, component.name())
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
        final ParameterSpec paramA = ParameterSpec.builder(concreteClassName, "elA", Modifier.FINAL)
                .addAnnotation(NULLABLE)
                .addJavadoc("The preferred input")
                .build();

        final ParameterSpec paramB = ParameterSpec.builder(concreteClassName, "elB", Modifier.FINAL)
                .addAnnotation(NULLABLE)
                .addJavadoc("The non-preferred input")
                .build();
        methodBuilder.addAnnotation(NULLABLE)
                .addParameter(paramA)
                .addParameter(paramB)
                .returns(concreteClassName)
                .addJavadoc("Merger for fields of class {@link $T}", concreteClassName)
                .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", OBJECTS)
                .addStatement("return elB")
                .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", OBJECTS)
                .addStatement("return elA")
                .endControlFlow()
                .addStatement("final $T combined = new $T()", concreteClassName, concreteClassName)
                .addStatement("combined.addAll(elA)")
                .addStatement("combined.addAll(elB)")
                // Since this goes via the builder, that will handle things like immutability
                .addStatement("return combined");
    }

    @Override
    public void addDiffRecordComponents(final TypeName innerType, final ToBeBuiltRecord recordBuilder) {
        recordBuilder.addParameterSpec(
                        ParameterSpec.builder(concreteClassName, "addedElements")
                                .addJavadoc("The elements added to the collection")
                                .build()
                )
                .addParameterSpec(
                        ParameterSpec.builder(concreteClassName, "removedElements")
                                .addJavadoc("The elements removed from the collection")
                                .build()
                )
                .addParameterSpec(
                        ParameterSpec.builder(concreteClassName, "elementsInCommon")
                                .addJavadoc("The elements in common between the two instances")
                                .build()
                );
    }

    @Override
    public void writeNullableAutoAddManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleAddMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        // No-op
    }

    @Override
    public void writeNullableAutoRemoveManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleRemoveMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        // No-op
    }

    @Override
    public void writeNonNullAutoAddManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleAddMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        // No-op
    }

    @Override
    public void writeNonNullAutoRemoveManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleRemoveMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        // No-op
    }

    @Override
    public void writeNullableImmutableAddManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleAddMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        // No-op
    }

    @Override
    public void writeNullableImmutableRemoveManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleRemoveMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        // No-op
    }

    @Override
    public void writeNonNullImmutableAddManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleAddMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        // No-op
    }

    @Override
    public void writeNonNullImmutableRemoveManyToBuilder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleRemoveMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        // No-op
    }
}

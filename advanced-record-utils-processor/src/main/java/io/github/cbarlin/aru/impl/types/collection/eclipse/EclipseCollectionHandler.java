package io.github.cbarlin.aru.impl.types.collection.eclipse;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.StandardCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.list.EclipseListCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.list.EclipsePrimitiveList;
import io.github.cbarlin.aru.impl.types.collection.eclipse.set.EclipsePrimitiveSet;
import io.github.cbarlin.aru.impl.types.collection.eclipse.set.EclipseSetCollectionHandler;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.*;

public sealed abstract class EclipseCollectionHandler
    extends StandardCollectionHandler
    permits EclipseListCollectionHandler,
            EclipseSetCollectionHandler,
            EclipsePrimitiveSet,
            EclipsePrimitiveList
{

    protected final ClassName factoryClassName;
    protected final ClassName classNameOnBuilder;

    protected EclipseCollectionHandler(
        final ClassName classNameOnComponent,
        final ClassName mutableClassName,
        final ClassName immutableClassName,
        final ClassName factoryClassName
    ) {
        super(classNameOnComponent, mutableClassName, immutableClassName);
        this.factoryClassName = factoryClassName;
        this.classNameOnBuilder = classNameOnComponent;
    }

    protected EclipseCollectionHandler(
            final ClassName classNameOnComponent,
            final ClassName mutableClassName,
            final ClassName immutableClassName,
            final ClassName factoryClassName,
            final ClassName classNameOnBuilder
    ) {
        super(classNameOnComponent, mutableClassName, immutableClassName);
        this.factoryClassName = factoryClassName;
        this.classNameOnBuilder = classNameOnBuilder;
    }

    @Override
    public void writeNullableAutoRetainAll(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String componentName = component.name();
        methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, componentName)
                .addStatement("this.$L = this.$L.select(__v -> $L.contains(__v))", componentName, componentName, componentName)
                .endControlFlow();
    }

    @Override
    public void writeNullableAutoAddSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String componentName = component.name();
        if (mutableClassName.equals(classNameOnBuilder)) {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, componentName)
                    .addStatement("this.$L = $T.mutable.of($L)", componentName, factoryClassName, componentName)
                    .nextControlFlow("else")
                    .addStatement("this.$L.add($L)", componentName, componentName)
                    .endControlFlow();
        } else if (immutableClassName.equals(classNameOnBuilder)) {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, componentName)
                    .addStatement("this.$L = $T.immutable.of($L)", componentName, factoryClassName, componentName)
                    .nextControlFlow("else")
                    .addStatement("this.$L = this.$L.newWith($L)", componentName, componentName, componentName)
                    .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, componentName)
                    .addStatement("this.$L = $T.mutable.of($L)", componentName, factoryClassName, componentName)
                    .nextControlFlow("else if (this.$L instanceof $T)", componentName, immutableClassName)
                    .addStatement("this.$L = this.$L.newWith($L)", componentName, componentName, componentName)
                    .nextControlFlow("else")
                    .addStatement("this.$L.add($L)", componentName, componentName)
                    .endControlFlow();
        }
    }

    @Override
    public void writeNullableAutoRemovePredicate(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String componentName = component.name();
        if (mutableClassName.equals(classNameOnBuilder)) {
            methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, componentName)
                    .addStatement("this.$L.removeIf($L)", componentName, componentName)
                    .endControlFlow();
        } else if (immutableClassName.equals(classNameOnBuilder)) {
            methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, componentName)
                    .addStatement("this.$L = this.$L.select(__v -> !$L.test(__v))", componentName, componentName, componentName)
                    .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, componentName)
                    .beginControlFlow("if (this.$L instanceof $T)", componentName, immutableClassName)
                    .addStatement("this.$L = this.$L.select(__v -> !$L.test(__v))", componentName, componentName, componentName)
                    .nextControlFlow("else")
                    .addStatement("this.$L.removeIf($L)", componentName, componentName)
                    .endControlFlow()
                    .endControlFlow();
        }
    }

    @Override
    public void writeNullableAutoRemoveSingle(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        final String componentName = component.name();
        if (mutableClassName.equals(classNameOnBuilder)) {
            methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, componentName)
                    .addStatement("this.$L.remove($L)", componentName, componentName)
                    .endControlFlow();
        } else if (immutableClassName.equals(classNameOnBuilder)) {
            methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, componentName)
                    .addStatement("this.$L = this.$L.newWithout($L)", componentName, componentName, componentName)
                    .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, componentName)
                    .beginControlFlow("if (this.$L instanceof $T)", componentName, immutableClassName)
                    .addStatement("this.$L = this.$L.newWithout($L)", componentName, componentName, componentName)
                    .nextControlFlow("else")
                    .addStatement("this.$L.remove($L)", componentName, componentName)
                    .endControlFlow()
                    .endControlFlow();
        }
    }

    protected static void writeMergerMethodImpl(final MethodSpec.Builder methodBuilder, final TypeName typeName) {
        final TypeName tn = typeName.annotated(AnnotationSpec.builder(NULLABLE).build());
        final ParameterSpec paramA = ParameterSpec.builder(tn, "elA", Modifier.FINAL)
            .addJavadoc("The preferred input")
            .build();

        final ParameterSpec paramB = ParameterSpec.builder(tn, "elB", Modifier.FINAL)
            .addJavadoc("The non-preferred input")
            .build();
        methodBuilder.addParameter(paramA)
                     .addParameter(paramB)
                     .returns(tn)
                     .addJavadoc("Merger for fields of class {@link $T}", typeName)
                     .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", OBJECTS)
                     .addStatement("return elB")
                     .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", OBJECTS)
                     .addStatement("return elA")
                     .endControlFlow()
                     .addStatement("return elA.newWithAll(elB)");
    }

    @Override
    public void writeNullableAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        if (classNameOnBuilder.equals(classNameOnComponent)) {
            super.writeNullableAutoSetter(component, methodBuilder, innerType, nullReplacesNotNull);
        } else {
            final String name = component.name();
            methodBuilder.beginControlFlow("if (!($T.isNull(this.$L) && $T.isNull($L)))", OBJECTS, name, OBJECTS, name);
            methodBuilder.beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, name);
            if (classNameOnBuilder.equals(immutableClassName)) {
                methodBuilder.addStatement("this.$L = $T.immutable.empty()", name, factoryClassName);
            } else {
                methodBuilder.addStatement("this.$L = $T.mutable.empty()", name, factoryClassName);
            }
            methodBuilder.endControlFlow();
            this.writeNonNullAutoSetter(component, methodBuilder, innerType, nullReplacesNotNull);
            methodBuilder.endControlFlow();
        }
    }

    @Override
    public void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        if (nullReplacesNotNull) {
            methodBuilder.addStatement("this.$L.clear()", component.name())
                    .beginControlFlow("if ($T.nonNull($L))", OBJECTS, component.name())
                    .addStatement("this.$L.addAllIterable($L)", component.name(), component.name())
                    .endControlFlow();
        } else {
            methodBuilder.beginControlFlow("if ($T.nonNull($L))", OBJECTS, component.name())
                    .addStatement("this.$L.clear()", component.name())
                    .addStatement("this.$L.addAllIterable($L)", component.name(), component.name())
                    .endControlFlow();
        }
    }

    @Override
    public void writeNullableAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        if (classNameOnBuilder.equals(classNameOnComponent)) {
            super.writeNullableAutoGetter(component, methodBuilder, innerType);
        } else {
            super.writeNullableImmutableGetter(component, methodBuilder, innerType);
        }
    }

    @Override
    public void writeNonNullAutoGetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType) {
        if (classNameOnBuilder.equals(classNameOnComponent)) {
            super.writeNonNullAutoGetter(component, methodBuilder, innerType);
        } else {
            super.writeNonNullImmutableGetter(component, methodBuilder, innerType);
        }
    }

    @Override
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder
            .addComment("Created in $L", this.getClass().getCanonicalName())
            .addStatement("final $T<$T> $L = $L.toImmutable()", immutableClassName, innerTypeName, assignmentName, fieldName);
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent ecc, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final ParameterizedTypeName ptn = ParameterizedTypeName.get(mutableClassName, innerType);
        final FieldSpec fSpec = FieldSpec.builder(ptn, ecc.name(), Modifier.PRIVATE)
            .addAnnotation(NON_NULL)
            .initializer("$T.mutable.empty()", factoryClassName)
            .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    public void writeNonNullAutoAddManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleAddMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        writeNonNullableEclipseAdders(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
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
        writeNonNullableEclipseAdders(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

    protected void writeNonNullableEclipseAdders(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleAddMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        writeEclipseIterableAdder(component, builder, innerType, singleAddMethodName, visitor);
        CollectionHandler.writeBasicIteratorOperator(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
        CollectionHandler.writeBasicSpliteratorOperator(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

    protected void writeEclipseIterableAdder(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final ParameterizedTypeName paramTn = ParameterizedTypeName.get(ITERABLE, innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
                .addJavadoc("An iterable to be merged into the collection")
                .addAnnotation(NOT_NULL)
                .build();
        final MethodSpec.Builder method = builder
                .createMethod(addAllMethodName, visitor.claimableOperation(), ITERABLE)
                .addJavadoc("Adds all elements of the provided iterable to {@code $L}", fieldName)
                .addParameter(param)
                .returns(builder.className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
                .addStatement("this.$L.addAllIterable($L)", fieldName, fieldName)
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
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
            .addStatement("final $T<$T> combined = $T.mutable.empty()", mutableClassName, innerType, factoryClassName)
            .addStatement("combined.addAllIterable(elA)")
            .addStatement("combined.addAllIterable(elB)");
        if (mutableClassName.equals(classNameOnComponent)) {
            methodBuilder.addStatement("return combined");
        } else {
            methodBuilder.addStatement("return combined.toImmutable()");
        }
    }
}

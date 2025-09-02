package io.github.cbarlin.aru.impl.types.collection.eclipse;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.StandardCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.list.EclipseListCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.set.EclipseSetCollectionHandler;
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
import static io.github.cbarlin.aru.impl.Constants.Names.ITERABLE;
import static io.github.cbarlin.aru.impl.Constants.Names.NON_NULL;

public sealed abstract class EclipseCollectionHandler extends StandardCollectionHandler permits EclipseListCollectionHandler, EclipseSetCollectionHandler {

    protected final ClassName factoryClassName;

    protected EclipseCollectionHandler(
        final ClassName classNameOnComponent,
        final ClassName mutableClassName,
        final ClassName immutableClassName,
        final ClassName factoryClassName
    ) {
        super(classNameOnComponent, mutableClassName, immutableClassName);
        this.factoryClassName = factoryClassName;
    }

    @Override
    public void writeNonNullAutoSetter(AnalysedComponent component, MethodSpec.Builder methodBuilder, TypeName innerType, boolean nullReplacesNotNull) {
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
    protected void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        methodBuilder.addStatement("final $T<$T> $L = $L.toImmutable()", immutableClassName, innerTypeName, assignmentName, fieldName);
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
    public void writeNonNullAutoAddManyToBuilder(AnalysedComponent component, ToBeBuilt builder, TypeName innerType, String singleAddMethodName, String addAllMethodName, AruVisitor<?> visitor) {
        writeEclipseAdders(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
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
        writeEclipseAdders(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
    }

    protected void writeEclipseAdders (
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String singleAddMethodName,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        writeEclipseIterableAdder(component, builder, innerType, singleAddMethodName, visitor);
        CollectionHandler.writeBasicIteratorAdder(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
        CollectionHandler.writeBasicSpliteratorAdder(component, builder, innerType, singleAddMethodName, addAllMethodName, visitor);
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
    public void writeMergerMethod(TypeName innerType, MethodSpec.Builder methodBuilder) {
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

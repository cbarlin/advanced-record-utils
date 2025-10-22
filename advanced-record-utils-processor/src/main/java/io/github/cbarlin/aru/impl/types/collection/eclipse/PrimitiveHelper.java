package io.github.cbarlin.aru.impl.types.collection.eclipse;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.Objects;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.*;
import static io.github.cbarlin.aru.impl.Constants.Names.ITERABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERABLE;

public final class PrimitiveHelper {
    
    private PrimitiveHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void writeNullableRemoveMany(
            final AnalysedComponent component,
            final ToBeBuilt builder,
            final TypeName innerType,
            final String addAllMethodName,
            final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final TypeName paramTn = ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERABLE.get(innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
                .addJavadoc("A collection to be removed from the current state")
                .addAnnotation(NOT_NULL)
                .build();
        final MethodSpec.Builder method = builder
                .createMethod(addAllMethodName, visitor.claimableOperation(), COLLECTION)
                .addJavadoc("Removes all elements of the provided collection from {@code $L}", fieldName)
                .addParameter(param)
                .returns(builder.className())
                .addAnnotation(NOT_NULL)
                .beginControlFlow("if ($T.nonNull(this.$L))", OBJECTS, fieldName)
                    .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
                        .addStatement("this.$L.removeAll($L)", fieldName, fieldName)
                    .endControlFlow()
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    public static void writeNonNullRemoveMany(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final TypeName paramTn = ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERABLE.get(innerType);
        final ParameterSpec param = ParameterSpec.builder(paramTn, fieldName, Modifier.FINAL)
                                                 .addJavadoc("A collection to be merged into the collection")
                                                 .addAnnotation(NOT_NULL)
                                                 .build();
        final MethodSpec.Builder method = builder
            .createMethod(addAllMethodName, visitor.claimableOperation(), COLLECTION)
            .addJavadoc("Adds all elements of the provided collection to {@code $L}", fieldName)
            .addParameter(param)
            .returns(builder.className())
            .addAnnotation(NOT_NULL)
            .beginControlFlow("if ($T.nonNull($L))", OBJECTS, fieldName)
            .addStatement("this.$L.removeAll($L)", fieldName, fieldName)
            .endControlFlow()
            .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    public static void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final boolean nullReplacesNotNull) {
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

    /**
     * Write "addAll" methods to the builder that is nullable and inherits the type passed to it
     */
    public static void writeNullableAdders(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String addAllMethodName,
        final AruVisitor<?> visitor,
        final ClassName factoryClassName
    ) {
        final String fieldName = component.name();
        final TypeName paramTn = ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERABLE.get(innerType);
        Objects.requireNonNull(paramTn, () -> "Cannot find an iterable for " + innerType);
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
                    .beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, fieldName)
                        .addStatement("this.$L = $T.mutable.ofAll($L)", fieldName, factoryClassName, fieldName)
                    .nextControlFlow("else")
                        .addStatement("this.$L.addAll($L)", fieldName, fieldName)
                    .endControlFlow()
                .endControlFlow()
                .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    public static void writeNonNullAdders(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        final String fieldName = component.name();
        final TypeName paramTn = ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERABLE.get(innerType);
        Objects.requireNonNull(paramTn, () -> "Cannot find an iterable for " + innerType);
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
            .addStatement("this.$L.addAll($L)", fieldName, fieldName)
            .endControlFlow()
            .addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, visitor);
    }

    public static void writeMergerMethod(final TypeName classNameOnComponent, final MethodSpec.Builder methodBuilder, final TypeName mutableClassName, final TypeName factoryClassName) {
        final ParameterSpec paramA = ParameterSpec.builder(classNameOnComponent, "elA", Modifier.FINAL)
                                                  .addAnnotation(NULLABLE)
                                                  .addJavadoc("The preferred input")
                                                  .build();

        final ParameterSpec paramB = ParameterSpec.builder(classNameOnComponent, "elB", Modifier.FINAL)
                                                  .addAnnotation(NULLABLE)
                                                  .addJavadoc("The non-preferred input")
                                                  .build();
        methodBuilder.addAnnotation(NULLABLE)
                     .addParameter(paramA)
                     .addParameter(paramB)
                     .returns(classNameOnComponent)
                     .addJavadoc("Merger for fields of class {@link $T}", classNameOnComponent)
                     .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", OBJECTS)
                     .addStatement("return elB")
                     .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", OBJECTS)
                     .addStatement("return elA")
                     .endControlFlow()
                     .addStatement("final $T combined = $T.mutable.empty()", mutableClassName, factoryClassName)
                     .addStatement("combined.addAll(elA)")
                     .addStatement("combined.addAll(elB)");
        if (mutableClassName.equals(classNameOnComponent)) {
            methodBuilder.addStatement("return combined");
        } else {
            methodBuilder.addStatement("return combined.toImmutable()");
        }
    }

    public static void addDiffRecordComponents(final TypeName immutableClassName, final ToBeBuiltRecord recordBuilder) {
        recordBuilder.addParameterSpec(
                         ParameterSpec.builder(immutableClassName, "addedElements")
                                      .addJavadoc("The elements added to the collection")
                                      .build()
                     )
                     .addParameterSpec(
                         ParameterSpec.builder(immutableClassName, "removedElements")
                                      .addJavadoc("The elements removed from the collection")
                                      .build()
                     )
                     .addParameterSpec(
                         ParameterSpec.builder(immutableClassName, "elementsInCommon")
                                      .addJavadoc("The elements in common between the two instances")
                                      .build()
                     );
    }
}

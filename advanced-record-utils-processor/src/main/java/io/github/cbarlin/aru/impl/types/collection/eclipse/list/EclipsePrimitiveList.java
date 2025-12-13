package io.github.cbarlin.aru.impl.types.collection.eclipse.list;

import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltRecord;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.impl.types.collection.eclipse.EclipseCollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.eclipse.PrimitiveHelper;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.Map;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.MATH;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERATOR;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.PRIMITIVE_FACTORY_PACKAGE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.PRIMITIVE_LIST_PACKAGE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.PRIMITIVE_MAP_PACKAGE;

public final class EclipsePrimitiveList extends EclipseCollectionHandler {

    private static final AnnotationSpec ANNO_NULLABLE = AnnotationSpec.builder(NULLABLE).build();

    public static final String FREQUENCY_MAP = """
        $T.requireNonNullElse($L, $T.immutable.of())
            .forEach(v -> $L.addToValue(v, 1))
        """.trim();

    private static final String BOOLEAN_DIFF_BLOCK = """
        final int oT = o.count(b -> b);
        final int oF = o.size() - oT;
        final int uT = u.count(b -> b);
        final int uF = u.size() - uT;
        final int cT = Math.min(oT, uT);
        final int cF = Math.min(oF, uF);
        for (int i = 0; i < cT; i++) {
            common.add(true);
        }
        for (int i = 0; i < cF; i++) {
            common.add(false);
        }
        if (oT > uT) {
            for (int i = 0; i < (oT - uT); i++) {
                removed.add(true);
            }
        } else {
            for (int i = 0; i < (uT - oT); i++) {
                added.add(true);
            }
        }
        if (oF > uF) {
            for (int i = 0; i < (oF - uF); i++) {
                removed.add(false);
            }
        } else {
            for (int i = 0; i < (uF - oF); i++) {
                added.add(false);
            }
        }
        """.trim();

    private final ClassName mutableSetName;

    public EclipsePrimitiveList(
        final ClassName classNameOnComponent,
        final ClassName mutableClassName,
        final ClassName immutableClassName,
        final ClassName factoryClassName,
        final ClassName mutableSetName
    ) {
        super(classNameOnComponent, mutableClassName, immutableClassName, factoryClassName, mutableClassName);
        this.mutableSetName = mutableSetName;
    }

    @Override
    public void writeNullableAutoRemoveManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleRemoveMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        PrimitiveHelper.writeNullableRemoveMany(
                component, builder, innerType, addAllMethodName, visitor
        );
    }

    @Override
    public void writeNullableImmutableRemoveManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleRemoveMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        PrimitiveHelper.writeNullableRemoveMany(
                component, builder, innerType, addAllMethodName, visitor
        );
    }

    @Override
    public void writeNullableImmutableAddManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleAddMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        PrimitiveHelper.writeNullableAdders(
                component, builder, innerType, addAllMethodName, visitor, factoryClassName
        );
    }

    @Override
    public void writeNullableAutoAddManyToBuilder(final AnalysedComponent component, final ToBeBuilt builder, final TypeName innerType, final String singleAddMethodName, final String addAllMethodName, final AruVisitor<?> visitor) {
        PrimitiveHelper.writeNullableAdders(
                component, builder, innerType, addAllMethodName, visitor, factoryClassName
        );
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
        PrimitiveHelper.writeNonNullRemoveMany(component, builder, innerType, addAllMethodName, visitor);
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
        PrimitiveHelper.writeNonNullRemoveMany(component, builder, innerType, addAllMethodName, visitor);
    }

    @Override
    protected void writeNonNullableEclipseAdders(
        final AnalysedComponent component,
        final ToBeBuilt builder,
        final TypeName innerType,
        final String singleAddMethodName,
        final String addAllMethodName,
        final AruVisitor<?> visitor
    ) {
        PrimitiveHelper.writeNonNullAdders(component, builder, innerType, addAllMethodName, visitor);
    }

    @Override
    protected final void convertToImmutable(final MethodSpec.Builder methodBuilder, final String fieldName, final String assignmentName, final TypeName innerTypeName) {
        if (classNameOnComponent.equals(mutableClassName)) {
            methodBuilder
                .addComment("Created in $L", this.getClass().getCanonicalName())
                .addStatement("final $T $L = $T.mutable.ofAll($L)", mutableClassName, assignmentName, factoryClassName, fieldName);
        } else {
            methodBuilder
                .addComment("Created in $L", this.getClass().getCanonicalName())
                .addStatement("final $T $L = $L.toImmutable()", immutableClassName, assignmentName, fieldName);
        }
    }

    @Override
    public void writeNonNullAutoSetter(final AnalysedComponent component, final MethodSpec.Builder methodBuilder, final TypeName innerType, final boolean nullReplacesNotNull) {
        PrimitiveHelper.writeNonNullAutoSetter(component, methodBuilder, nullReplacesNotNull);
    }

    @Override
    public boolean canHandle(final AnalysedComponent component) {
        return component.className()
                .filter(classNameOnComponent::equals)
                .isPresent();
    }

    @Override
    public void addNonNullAutoField(final AnalysedComponent ecc, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final FieldSpec fSpec = FieldSpec.builder(mutableClassName, ecc.name(), Modifier.PRIVATE)
                                         .initializer("$T.mutable.empty()", factoryClassName)
                                         .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    public void addNullableAutoField(final AnalysedComponent ecc, final ToBeBuilt addFieldTo, final TypeName innerType) {
        final FieldSpec fSpec = FieldSpec.builder(mutableClassName.annotated(CommonsConstants.NULLABLE_ANNOTATION), ecc.name(), Modifier.PRIVATE)
                .build();
        addFieldTo.addField(fSpec);
    }

    @Override
    public final void addDiffRecordComponents(final TypeName innerType, final ToBeBuiltRecord recordBuilder) {
        PrimitiveHelper.addDiffRecordComponents(immutableClassName, recordBuilder);
    }

    @Override
    public final void writeMergerMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder) {
        PrimitiveHelper.writeMergerMethod(classNameOnComponent, methodBuilder, mutableClassName, factoryClassName);
    }

    private static final Map<TypeName, ClassName> MP_LONG_FACTORY = Map.of(
        TypeName.BYTE, ClassName.get(PRIMITIVE_FACTORY_PACKAGE, "ByteLongMaps"),
        TypeName.CHAR, ClassName.get(PRIMITIVE_FACTORY_PACKAGE, "CharLongMaps"),
        TypeName.DOUBLE, ClassName.get(PRIMITIVE_FACTORY_PACKAGE, "DoubleLongMaps"),
        TypeName.FLOAT, ClassName.get(PRIMITIVE_FACTORY_PACKAGE, "FloatLongMaps"),
        TypeName.INT, ClassName.get(PRIMITIVE_FACTORY_PACKAGE, "IntLongMaps"),
        TypeName.LONG, ClassName.get(PRIMITIVE_FACTORY_PACKAGE, "LongLongMaps"),
        TypeName.SHORT, ClassName.get(PRIMITIVE_FACTORY_PACKAGE, "ShortLongMaps")
    );

    private static final Map<TypeName, ClassName> MP_LONG_TYPE = Map.of(
        TypeName.BYTE, ClassName.get(PRIMITIVE_MAP_PACKAGE, "MutableByteLongMap"),
        TypeName.CHAR, ClassName.get(PRIMITIVE_MAP_PACKAGE, "MutableCharLongMap"),
        TypeName.DOUBLE, ClassName.get(PRIMITIVE_MAP_PACKAGE, "MutableDoubleLongMap"),
        TypeName.FLOAT, ClassName.get(PRIMITIVE_MAP_PACKAGE, "MutableFloatLongMap"),
        TypeName.INT, ClassName.get(PRIMITIVE_MAP_PACKAGE, "MutableIntLongMap"),
        TypeName.LONG, ClassName.get(PRIMITIVE_MAP_PACKAGE, "MutableLongLongMap"),
        TypeName.SHORT, ClassName.get(PRIMITIVE_MAP_PACKAGE, "MutableShortLongMap")
    );

    private static void writeBooleanDifferMethod(final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord, final ClassName mutableClassName, final ClassName factoryClassName) {
        final TypeName genericList = ClassName.get(PRIMITIVE_LIST_PACKAGE, "BooleanList");
        methodBuilder.addModifiers(Modifier.FINAL, Modifier.STATIC)
            .returns(collectionResultRecord)
            .addParameter(
                ParameterSpec.builder(genericList.annotated(ANNO_NULLABLE), "original", Modifier.FINAL)
                             .build()
            )
            .addParameter(
                ParameterSpec.builder(genericList.annotated(ANNO_NULLABLE), "updated", Modifier.FINAL)
                             .build()
            )
            .addStatement("final $T common = $T.mutable.empty()", mutableClassName, factoryClassName)
            .addStatement("final $T added = $T.mutable.empty()", mutableClassName, factoryClassName)
            .addStatement("final $T removed = $T.mutable.empty()", mutableClassName, factoryClassName)
            .addStatement("final $T o = $T.requireNonNullElse(original, $T.immutable.empty())", genericList, OBJECTS, factoryClassName)
            .addStatement("final $T u = $T.requireNonNullElse(updated, $T.immutable.empty())", genericList, OBJECTS, factoryClassName)
            .addCode(BOOLEAN_DIFF_BLOCK + "\n")
            .addStatement("return new $T(added.toImmutable(), common.toImmutable(), removed.toImmutable())", collectionResultRecord);
    }
    
    @Override
    public final void writeDifferMethod(final TypeName innerType, final MethodSpec.Builder methodBuilder, final ClassName collectionResultRecord) {
        if (TypeName.BOOLEAN.equals(innerType)) {
            writeBooleanDifferMethod(methodBuilder, collectionResultRecord, mutableClassName, factoryClassName);
            return;
        }
        final ClassName mapType = MP_LONG_TYPE.get(innerType);
        final ClassName mapFactory = MP_LONG_FACTORY.get(innerType);
        final ClassName iteratorType = ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERATOR.get(innerType);

        methodBuilder.addModifiers(Modifier.FINAL, Modifier.STATIC)
                .returns(collectionResultRecord)
                .addParameter(
                        ParameterSpec.builder(classNameOnComponent.annotated(ANNO_NULLABLE), "original", Modifier.FINAL)
                                .build()
                )
                .addParameter(
                        ParameterSpec.builder(classNameOnComponent.annotated(ANNO_NULLABLE), "updated", Modifier.FINAL)
                                .build()
                )
                .addComment("Create frequency maps to count occurrences")
                .addStatement("final $T $L = $T.mutable.empty()", mapType, "originalFreq", mapFactory)
                .addStatement("final $T $L = $T.mutable.empty()", mapType, "updatedFreq", mapFactory)
                .addStatement(
                    FREQUENCY_MAP,
                    OBJECTS,
                    "original",
                    factoryClassName,
                    "originalFreq"
                )
                .addStatement(
                    FREQUENCY_MAP,
                    OBJECTS,
                    "updated",
                    factoryClassName,
                    "updatedFreq"
                )
                .addStatement("final $T added = $T.mutable.of()", mutableClassName, factoryClassName)
                .addStatement("final $T removed = $T.mutable.of()", mutableClassName, factoryClassName)
                .addStatement("final $T common = $T.mutable.of()", mutableClassName, factoryClassName)
                .addComment("Obtain unique elements")
                .addStatement("final $T allUniqueElements = originalFreq.keysView().toSet()", mutableSetName)
                .addStatement("allUniqueElements.addAll(updatedFreq.keysView().toSet())")
                .addStatement("final $T iterator = allUniqueElements.$LIterator()", iteratorType, innerType.toString())
                .beginControlFlow("while (iterator.hasNext())")
                .addStatement("final $T element = iterator.next()", innerType)
                .addStatement("final long originalCount = originalFreq.getIfAbsent(element, 0L)")
                .addStatement("final long updatedCount = updatedFreq.getIfAbsent(element, 0L)")
                .addStatement("final long commonCount = $T.min(originalCount, updatedCount)", MATH)

                .beginControlFlow("for (long i = 0; i < commonCount; i++)")
                .addStatement("common.add(element)")
                .endControlFlow()

                .beginControlFlow("if (originalCount > updatedCount)")
                .addStatement("final long removedCount = originalCount - updatedCount")
                .beginControlFlow("for (long i = 0; i < removedCount; i++)")
                .addStatement("removed.add(element)")
                .endControlFlow()

                .nextControlFlow("else if (updatedCount > originalCount)")
                .addStatement("final long addedCount = updatedCount - originalCount")
                .beginControlFlow("for (long i = 0; i < addedCount; i++)")
                .addStatement("added.add(element)")
                .endControlFlow()
                .endControlFlow()

                .endControlFlow()
                .addStatement(
                   "return new $T(added.toImmutable(), common.toImmutable(), removed.toImmutable())",
                    collectionResultRecord
                );
    }
}

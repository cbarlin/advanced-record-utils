package io.github.cbarlin.aru.impl.types.dependencies;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_COLLECTION;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__IMMUTABLE_SET;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_COLLECTION;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_LIST;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_SET;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.impl.types.OptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

/**
 * Handles record components typed as {@code Optional<ImmutableList<T>>} and similar Eclipse Collections types.
 * <p>
 * This class specializes in extracting information and unwrapping Eclipse Collections
 *   types enclosed within Java's Optional container. It supports various Eclipse collection
 *   implementations including immutable/mutable lists and sets.
 * <p>
 * Analysis and construction is performed in the EclipseOptionalCollectionAnalyser
 */
public final class EclipseAnalysedOptionalCollection extends AnalysedComponent implements OptionalComponent<EclipseAnalysedOptionalCollection> {

    private static final String COLLECTION_ELEMENT = "__collectionElement";
    private final DeclaredType collectionTypeMirror;
    private final TypeName collectionTypeName;
    private final ClassName erasedCollectionTypeName;
    private final TypeMirror innerType;
    private final TypeName innerTypeName;

    /**
     * Note: The constructor will *not* validate the arguments passed to it.
     * <p>
     * It is expected that the validation, including the ability to read type arguments, is
     *   done externally
     */
    public EclipseAnalysedOptionalCollection(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord,
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        super(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        final DeclaredType declOuter = (DeclaredType) componentType;
        final List<? extends TypeMirror> typeArguments = declOuter.getTypeArguments();
        collectionTypeMirror = (DeclaredType) typeArguments.get(0);
        collectionTypeName = TypeName.get(collectionTypeMirror);
        erasedCollectionTypeName = (ClassName) TypeName.get(APContext.types().erasure(collectionTypeMirror));

        // OK, let's extract the type inside that!
        final List<? extends TypeMirror> collTypeArgs = collectionTypeMirror.getTypeArguments();
        innerType = collTypeArgs.get(0);
        innerTypeName = TypeName.get(innerType);
    }

    public boolean isImmutableCollection() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, ECLIPSE_COLLECTIONS__IMMUTABLE_COLLECTION);
    }

    public boolean isMutableCollection() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, ECLIPSE_COLLECTIONS__MUTABLE_COLLECTION);
    }

    public boolean isImmutableList() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, ECLIPSE_COLLECTIONS__IMMUTABLE_LIST);
    }

    public boolean isMutableList() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, ECLIPSE_COLLECTIONS__MUTABLE_LIST);
    }

    public boolean isList() {
        return isImmutableList() || isMutableList();
    }

    public boolean isImmutableSet() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, ECLIPSE_COLLECTIONS__IMMUTABLE_SET);
    }

    public boolean isMutableSet() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, ECLIPSE_COLLECTIONS__MUTABLE_SET);
    }

    public boolean isSet() {
        return isImmutableSet() || isMutableSet();
    }

    @Override
    public TypeName typeName() {
        return ParameterizedTypeName.get(OPTIONAL, collectionTypeName);
    }

    @Override
    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.of(OPTIONAL);
    }

    @Override
    public TypeMirror unNestedPrimaryComponentType() {
        return innerType;
    }

    @Override
    public TypeName unNestedPrimaryTypeName() {
        return innerTypeName;
    }

    @Override
    public boolean requiresUnwrapping() {
        return true;
    }

    @Override
    public boolean isLoopable() {
        return true;
    }

    @Override
    public TypeName serialisedTypeName() {
        return ParameterizedTypeName.get(LIST, innerTypeName);
    }

    /**
     * Returns the erased type name of the collection (without generic parameters).
     * @return the raw ClassName of the collection without type parameters
     */
    public ClassName erasedCollectionTypeName() {
        return erasedCollectionTypeName;
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedTypeName) {
        methodBuilder.beginControlFlow("if ($L.isPresent())", incomingName)
            .beginControlFlow("for (final $T $L : $L.get())", innerTypeName, COLLECTION_ELEMENT, incomingName);
        withUnwrappedName.accept(COLLECTION_ELEMENT);
        methodBuilder.endControlFlow()
            .endControlFlow();
    }

    @Override
    public EclipseAnalysedOptionalCollection component() {
        return this;
    }
}

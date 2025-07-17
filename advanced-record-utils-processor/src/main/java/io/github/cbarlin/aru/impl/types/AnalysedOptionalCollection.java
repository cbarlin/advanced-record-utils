package io.github.cbarlin.aru.impl.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;

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
 * Handles e.g. {@code Optional<List<T>>}
 */
public final class AnalysedOptionalCollection extends AnalysedComponent implements OptionalComponent<AnalysedOptionalCollection> {

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
    public AnalysedOptionalCollection(
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

    public boolean isList() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, LIST);
    }

    public boolean isSet() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, SET);
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
    public AnalysedOptionalCollection component() {
        return this;
    }
}

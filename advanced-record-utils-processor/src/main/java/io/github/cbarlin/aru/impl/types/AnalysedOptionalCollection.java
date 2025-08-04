package io.github.cbarlin.aru.impl.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;

import java.util.Optional;
import java.util.function.Consumer;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

/**
 * Handles e.g. {@code Optional<List<T>>}
 */
public record AnalysedOptionalCollection(
    AnalysedComponent delegate,
    DeclaredType collectionTypeMirror,
    TypeName collectionTypeName,
    ClassName erasedCollectionTypeName,
    TypeMirror innerType,
    TypeName innerTypeName
) implements DelegatingComponent {
    private static final String COLLECTION_ELEMENT = "__collectionElement";

    public boolean isList() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, LIST);
    }

    public boolean isSet() {
        return OptionalClassDetector.checkSameOrSubType(collectionTypeName, SET);
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

}
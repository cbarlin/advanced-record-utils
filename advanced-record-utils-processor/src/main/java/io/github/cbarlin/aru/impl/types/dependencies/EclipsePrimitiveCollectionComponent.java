package io.github.cbarlin.aru.impl.types.dependencies;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.types.AnalysedPrimitiveCollectionComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERATOR;

public final class EclipsePrimitiveCollectionComponent extends AnalysedPrimitiveCollectionComponent {

    private static final String UNWRAPPING_VARIABLE_NAME = "__innerValue";
    private static final String UNWRAPPING_ITERABLE_NAME = "__iter";

    private final ClassName iteratorClassName;

    public EclipsePrimitiveCollectionComponent(
        final AnalysedComponent delegate,
        final TypeMirror innerType,
        final TypeName innerTypeName,
        final ClassName erasedWrapperClassName
    ) {
        super(delegate, innerType, innerTypeName, erasedWrapperClassName);
        iteratorClassName = Objects.requireNonNull(ECLIPSE_COLLECTIONS_EXCHANGE__PRIMITIVE_ITERATOR.get(innerTypeName), "Must have a mapping to an iterator for a primitive collection");
    }

    @Override
    public Optional<ClassName> className() {
        return Optional.of(erasedWrapperClassName);
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder) {
        withinUnwrapped(withUnwrappedName, methodBuilder, name());
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName) {
        withinUnwrapped(withUnwrappedName, methodBuilder, incomingName, unNestedPrimaryTypeName());
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedType) {
        methodBuilder.addStatement("final $T $L = $L.$LIterator()", iteratorClassName, UNWRAPPING_ITERABLE_NAME, incomingName, innerTypeName.withoutAnnotations().toString());
        methodBuilder.beginControlFlow("while ($L.hasNext())", UNWRAPPING_ITERABLE_NAME);
        methodBuilder.addStatement("final $T $L = $L.next()", innerTypeName, UNWRAPPING_VARIABLE_NAME, UNWRAPPING_ITERABLE_NAME);
        withUnwrappedName.accept(UNWRAPPING_VARIABLE_NAME);
        methodBuilder.endControlFlow();
    }
}

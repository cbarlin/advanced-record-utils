package io.github.cbarlin.aru.impl.types.dependencies;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.function.Consumer;

public final class EclipseAnalysedCollectionComponent extends AnalysedCollectionComponent {

    private static final String UNWRAPPING_VARIABLE_NAME = "__innerValue";

    public EclipseAnalysedCollectionComponent(
            final AnalysedComponent delegate,
            final TypeMirror innerType,
            final TypeName innerTypeName,
            final ClassName erasedWrapperClassName
    ) {
        super(delegate, innerType, innerTypeName, erasedWrapperClassName);
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
        methodBuilder.beginControlFlow("for (final $T $L : $L)", unwrappedType, UNWRAPPING_VARIABLE_NAME, incomingName);
        withUnwrappedName.accept(UNWRAPPING_VARIABLE_NAME);
        methodBuilder.endControlFlow();
    }

}

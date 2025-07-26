package io.github.cbarlin.aru.core.types.components;

import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AnalysedCollectionComponent implements DelegatingComponent {

    private static final String UNWRAPPING_VARIABLE_NAME = "__innerValue";
    protected final AnalysedComponent delegate;
    protected final TypeMirror innerType;
    protected final TypeName innerTypeName;
    protected final ClassName erasedWrapperClassName;

    public AnalysedCollectionComponent(
            AnalysedComponent delegate,
            TypeMirror innerType,
            TypeName innerTypeName,
            ClassName erasedWrapperClassName
    ) {
        this.delegate = delegate;
        this.innerType = innerType;
        this.innerTypeName = innerTypeName;
        this.erasedWrapperClassName = erasedWrapperClassName;
    }

    public AdvRecUtilsSettings settings() {
        return delegate.parentRecord().settings();
    }

    @Override
    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.of(erasedWrapperClassName);
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
        // Always consider a collection of any type a simple list
        return ParameterizedTypeName.get(Names.LIST, innerTypeName);
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

    @Override
    public final String toString() {
        return delegate().toString();
    }

    @Override
    public AnalysedComponent delegate() {
        return delegate;
    }

    public TypeMirror innerType() {
        return innerType;
    }

    public TypeName innerTypeName() {
        return innerTypeName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AnalysedCollectionComponent) obj;
        return Objects.equals(this.delegate, that.delegate) &&
                Objects.equals(this.innerType, that.innerType) &&
                Objects.equals(this.innerTypeName, that.innerTypeName) &&
                Objects.equals(this.erasedWrapperClassName, that.erasedWrapperClassName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate, innerType, innerTypeName, erasedWrapperClassName);
    }

}

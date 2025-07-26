package io.github.cbarlin.aru.core.types.components;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

public non-sealed interface DelegatingComponent extends AnalysedComponent {

    AnalysedComponent delegate();

    @Override
    default boolean isClaimed(final ClaimableOperation operation) {
        return delegate().isClaimed(operation);
    }

    @Override
    default boolean attemptToClaim(final RecordVisitor visitor) {
        return delegate().attemptToClaim(visitor);
    }

    @Override
    default void retractClaim(final RecordVisitor visitor) {
        delegate().retractClaim(visitor);
    }

    @Override
    default ToBeBuilt generationArtifact(final String generatedClassName, final String generatedWrapperClassName, final ClaimableOperation claimableOperation) {
        return delegate().generationArtifact(generatedClassName, generatedWrapperClassName, claimableOperation);
    }

    @Override
    default void addCrossReference(final ProcessingTarget other) {
        delegate().addCrossReference(other);
    }

    @Override
    default void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder) {
        delegate().withinUnwrapped(withUnwrappedName, methodBuilder);
    }

    @Override
    default void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName) {
        delegate().withinUnwrapped(withUnwrappedName, methodBuilder, incomingName);
    }

    @Override
    default void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedTypeName) {
        delegate().withinUnwrapped(withUnwrappedName, methodBuilder, incomingName, unwrappedTypeName);
    }

    @Override
    default AnalysedRecord parentRecord() {
        return delegate().parentRecord();
    }

    @Override
    default RecordComponentElement element() {
        return delegate().element();
    }

    @Override
    default String name() {
        return delegate().name();
    }

    @Override
    default boolean isIntendedConstructorParam() {
        return delegate().isIntendedConstructorParam();
    }

    @Override
    default String nameFirstLetterCaps() {
        return delegate().nameFirstLetterCaps();
    }

    @Override
    default TypeMirror componentType() {
        return delegate().componentType();
    }

    @Override
    default boolean requiresUnwrapping() {
        return delegate().requiresUnwrapping();
    }

    @Override
    default boolean isLoopable() {
        return delegate().isLoopable();
    }

    @Override
    default TypeMirror unNestedPrimaryComponentType() {
        return delegate().unNestedPrimaryComponentType();
    }

    @Override
    default TypeMirror unNestedSecondaryComponentType() {
        return delegate().unNestedSecondaryComponentType();
    }

    @Override
    default TypeName typeName() {
        return delegate().typeName();
    }

    @Override
    default TypeName serialisedTypeName() {
        return delegate().serialisedTypeName();
    }

    @Override
    default TypeName unNestedPrimaryTypeName() {
        return delegate().unNestedPrimaryTypeName();
    }

    @Override
    default TypeName unNestedSecondaryTypeName() {
        return delegate().unNestedSecondaryTypeName();
    }

    @Override
    default Optional<ProcessingTarget> targetAnalysedType() {
        return delegate().targetAnalysedType();
    }

    @Override
    default Optional<ClassName> className() {
        return delegate().className();
    }

    @Override
    default Optional<ClassName> erasedWrapperTypeName() {
        return delegate().erasedWrapperTypeName();
    }

    @Override
    default List<AnalysedTypeConverter> converters() {
        return delegate().converters();
    }

    @Override
    default boolean isPrismPresent(ClassName annotationClassName, Class<?> prismClass) {
        return delegate().isPrismPresent(annotationClassName, prismClass);
    }

    @Override
    default <T> Optional<T> findPrism(ClassName annotationClassName, Class<T> prismClass) {
        return delegate().findPrism(annotationClassName, prismClass);
    }
}

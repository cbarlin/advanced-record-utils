package io.github.cbarlin.aru.core.types;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.core.types.components.DelegatingComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Consumer;

public sealed interface AnalysedComponent permits BasicAnalysedComponent, DelegatingComponent {

    /**
     * Check if something has been claimed. Useful if you are working on e.g.
     *  a Wither and want to ensure that a builder method exists
     */
    boolean isClaimed(final ClaimableOperation operation);

     /**
     * Attempt to claim an operation
     * @return true if claimed, or if the visitor already claimed the operation, and that it should continue processing
     */
    boolean attemptToClaim(final RecordVisitor visitor);

    /**
     * Retract a claim on an operation.
     * <p>
     * Retracting a claim that you do not hold will deliberately cause compilation to fail.
     * @param visitor The visitor retracting its claim
     */
    void retractClaim(final RecordVisitor visitor);

    /**
     * Obtain an existing or create a new generation artifact. Component-level generated classes (e.g. "StagedBuilder")
     *  are nested inside a wrapping class before being placed into the {@code *Utils} class
     * <p>
     * E.g. a "StagedBuilder" implementation for an "age" component of a record named "Person" will (probably) have a full class name of {@code PersonUtils.StagedBuilder.AgeStage}
     * <p>
     * All wrapping is done automatically.
     * <p>
     * All generated classes must have {@code @Generated} annotations, so one must be created for this item <em>and</em> the class-wide one
     * @param generatedClassName The name of the class when the source code is generated
     * @param generatedWrapperClassName The name of the wrapping class when the source code is generated
     * @param claimableOperation The operation for which the sub class is being build
     * 
     * @return The artifact requested
     */
    ToBeBuilt generationArtifact(final String generatedClassName, final String generatedWrapperClassName, final ClaimableOperation claimableOperation);

    /**
     * Add a cross-reference with another {@link ProcessingTarget} that is relevant to the current one
     * <p>
     * Cross-references are for when one {@code *Utils} class calls something in another. It should <strong>not</strong> be
     *   confused with classes the target class references (although usually all of those will be referenced).
     */
    void addCrossReference(final ProcessingTarget other);

    /**
     * Perform some operation within an "Unwrapped" version of the value
     * <p>
     * This can be used on items like {@code List<?>} or {@code Optional<?>} or even {@code Optional<List<?>>}
     * 
     * @param withUnwrappedName The name of the variable that this component has been unwrapped into
     * @param methodBuilder The builder that can be used to do the unwrapping
     */
    default void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder) {
        withinUnwrapped(withUnwrappedName, methodBuilder, name());
    }

    /**
     * Perform some operation within an "Unwrapped" version of the value
     * <p>
     * This can be used on items like {@code List<?>} or {@code Optional<?>} or even {@code Optional<List<?>>}
     * 
     * @param withUnwrappedName The name of the variable that this component has been unwrapped into
     * @param methodBuilder The builder that can be used to do the unwrapping
     * @param incomingName The current name of the variable.
     */
    default void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName) {
        withinUnwrapped(withUnwrappedName, methodBuilder, incomingName, unNestedPrimaryTypeName());
    }

    /**
     * Perform some operation within an "Unwrapped" version of the value
     * <p>
     * This can be used on items like {@code List<?>} or {@code Optional<?>} or even {@code Optional<List<?>>}
     * 
     * @param withUnwrappedName The name of the variable that this component has been unwrapped into
     * @param methodBuilder The builder that can be used to do the unwrapping
     * @param incomingName The current name of the variable.
     * @param unwrappedTypeName The type to use while unwrapping. Override with caution - unless you are using something as a parent type (e.g. String as CharSequence), prefer to omit this argument
     */
    default void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedTypeName) {
        APContext.messager().printError("Attempt to invoke an unwrapping on a type that doesn't require it", element());
        throw new UnsupportedOperationException("Cannot unwrap a type that isn't wrapped");
    }

    // Accessors below

    AnalysedRecord parentRecord();

    RecordComponentElement element();

    String name();

    TypeMirror componentType();

    TypeName typeName();

    Optional<ProcessingTarget> targetAnalysedType();

    // Defaults
    default boolean requiresUnwrapping() {
        return false;
    }

    default boolean isLoopable() {
        return false;
    }

    default boolean isIntendedConstructorParam() {
        return false;
    }

    default String nameFirstLetterCaps() {
        final String lcaseName = name();
        final String firstLetterCaps = StringUtils.toRootUpperCase(lcaseName.substring(0, 1));
        return firstLetterCaps + lcaseName.substring(1);
    }

    /**
     * Un-nested type. For normal references, it will be the same as {@link #componentType()}
     * <p>
     * For lists, this will be the type within the list
     * <p>
     * For maps, this will be the "Value" of Map[Key, Value]
     */
    default TypeMirror unNestedPrimaryComponentType() {
        return componentType();
    }

    /**
     * Un-nested type. For normal references, it will be the same as {@link #componentType()}
     * <p>
     * For lists, this will be the type within the list (same as {@link #unNestedPrimaryComponentType()})
     * <p>
     * For maps, this will be the "Key" of Map[Key, Value]
     */
    default TypeMirror unNestedSecondaryComponentType() {
        return unNestedPrimaryComponentType();
    }

    /**
     * When serialising, consider the component to be of this type
     */
    default TypeName serialisedTypeName() {
        return typeName();
    }

    /**
     * @see #unNestedPrimaryComponentType()
     */
    default TypeName unNestedPrimaryTypeName() {
        return typeName();
    }

    /**
     * @see #unNestedSecondaryComponentType()
     */
    default TypeName unNestedSecondaryTypeName() {
        return unNestedPrimaryTypeName();
    }

    default Optional<ClassName> className() {
        return targetAnalysedType()
            .map(ProcessingTarget::typeElement)
            .map(ClassName::get);
    }

    default Optional<ClassName> erasedWrapperTypeName() {
        return Optional.empty();
    }

    @Override
    String toString();
}

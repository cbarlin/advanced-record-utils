package io.github.cbarlin.aru.core.artifacts;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeSpec;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.function.Consumer;

public interface IToBeBuilt<T extends IToBeBuilt<T>> {

    T delegate();

    /**
     * Determine if this artifact actually has any content
     */
    default boolean hasContent() {
        return delegate().hasContent();
    }

    default ClassName className() {
        return delegate().className();
    }

    /**
     * Finishes up the class (or interface, or record) that we are building.
     * <p>
     * Note: While this method does attempt to force some sort of order on the things
     *   inside the generated Type, the TypeSpec has it's own ideas as to the order it wants to write things.
     * <p>
     * So the ordering we inject fills the gaps of the TypeSpec ones (because the ordering it uses doesn't enforce reproduceability...), 
     *    but it does override our ordering (e.g. the order of sub-classes :/)
     */
    default TypeSpec finishClass() {
        return delegate().finishClass();
    }

    default TypeSpec.Builder builder() {
        return delegate().builder();
    }

    default T addField(final FieldSpec fieldSpec) {
        return delegate().addField(fieldSpec);
    }

    /**
     * Add a logger to the class using own class name. Calling multiple times will have no effect beyond the first.
     * <p>
     * Note: Callers should check if loggers should be added to this class
     */
    default T addLogger() {
        return delegate().addLogger();
    }

    /**
     * Add a logger to the class, using the provided class name. Calling multiple times will have no effect beyond the first.
     * <p>
     * Note: Callers should check if loggers should be added to this class
     */
    default T addLogger(final ClassName loggerReferenceClass) {
        return delegate().addLogger(loggerReferenceClass);
    }

    /**
     * Create a child record artifact of this one. This will create a nested class structure for us
     * <p>
     * If you call this you are required to do things like put any annotations on etc.
     * 
     * @param generatedCodeName The name of the nested class that is actually generated. Do not include parent class names - nesting is handled for you
     * @param claimableOperation The claim that represents creating the method
     * @return
     */
    @NonNull
    default T childRecordArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return delegate().childRecordArtifact(generatedCodeName, claimableOperation);
    }

    /**
     * Create a child class artifact of this one. This will create a nested class structure for us
     * <p>
     * If you call this you are required to do things like put any annotations on etc.
     * 
     * @param generatedCodeName The name of the nested class that is actually generated. Do not include parent class names - nesting is handled for you
     * @param claimableOperation The claim that represents creating the method
     * @return
     */
    @NonNull
    default T childClassArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return delegate().childClassArtifact(generatedCodeName, claimableOperation);
    }

    /**
     * Create a child interface artifact of this one. This will create a nested class structure for us
     * <p>
     * If you call this you are required to do things like put any annotations on etc.
     * 
     * @param generatedCodeName The name of the nested class that is actually generated. Do not include parent class names - nesting is handled for you
     * @param claimableOperation The claim that represents creating the method
     * @return
     */
    @NonNull
    default T childInterfaceArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return delegate().childInterfaceArtifact(generatedCodeName, claimableOperation);
    }

    /**
     * Obtain a child artifact of any kind
     */
    default @Nullable T childArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return delegate().childArtifact(generatedCodeName, claimableOperation);
    }

    /**
     * Visit all the child artifacts
     */
    default void visitChildArtifacts(final Consumer<T> consumer) {
        delegate().visitChildArtifacts(consumer);
    }

    /**
     * Creates a public method on the generated class
     * <p>
     * This method only generates the stub and associates it, if it didn't already exist. Anything that calls this method is required to 
     *   do things like put any annotations on etc.
     * 
     * @param generatedCodeName The name of the method that is actually generated
     * @param claimableOperation The claim that is creating the method
     * @return A builder for the method
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return delegate().createMethod(generatedCodeName, claimableOperation);
    }

    /**
     * Creates a public method on the generated class for an intended modifier
     * <p>
     * This method only generates the stub and associates it, if it didn't already exist. Anything that calls this method is required to 
     *   do things like put any annotations on etc.
     * 
     * @param generatedCodeName The name of the method that is actually generated
     * @param claimableOperation The claim that is creating the method
     * @return A builder for the method
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Modifier modifier) {
        return delegate().createMethod(generatedCodeName, claimableOperation, modifier);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link AnalysedType}
     * <p>
     * E.g. <code>createMethod("addBook", "adderFluent", bookAnalysedType)</code>
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final AnalysedType targetAnalysedType) {
        return delegate().createMethod(generatedCodeName, claimableOperation, targetAnalysedType);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link Element}
     * <p>
     * E.g. <code>createMethod("addBook", "adderSingle", bookElement)</code>
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final AnalysedTypeConverter element) {
        return delegate().createMethod(generatedCodeName, claimableOperation, element);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link Element}
     * <p>
     * E.g. <code>createMethod("addBook", "adderSingle", bookElement)</code>
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element) {
        return delegate().createMethod(generatedCodeName, claimableOperation, element);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link Element} and ClassName
     * <p>
     * E.g. <code>createMethod("addBook", "adderSingle", bookElement)</code>
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element, final ClassName className) {
        return delegate().createMethod(generatedCodeName, claimableOperation, element, className);
    }


    /**
     * Creates a public method on the generated class that's made for a specific {@link Element} and ClassName
     * <p>
     * E.g. <code>createMethod("addBook", "adderSingle", bookElement)</code>
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element, final ParameterizedTypeName ptn) {
        return delegate().createMethod(generatedCodeName, claimableOperation, element, ptn);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link ClassName}
     * <p>
     * E.g. <code>createMethod("addBook", "adderAllIterable", bookClassName)</code>
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final ClassName className) {
        return delegate().createMethod(generatedCodeName, claimableOperation, className);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link ClassName}
     * <p>
     * E.g. <code>createMethod("addBook", "adderAllIterable", bookClassName)</code>
     */
    default MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final ParameterizedTypeName paramTn) {
        return delegate().createMethod(generatedCodeName, claimableOperation, paramTn);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link AnalysedComponent}
     * <p>
     * E.g. <code>createMethod("addBook", "adderAllIterable", analysedComponent)</code>
     */
    default MethodSpec.Builder createMethod(final String name, final ClaimableOperation claimableOperation, final AnalysedComponent analysedComponent) {
        return delegate().createMethod(name, claimableOperation, analysedComponent);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link AnalysedComponent} and {@link ClassName}
     * <p>
     * E.g. <code>createMethod("addBook", "adderAllIterable", analysedComponent)</code>
     */
    default MethodSpec.Builder createMethod(final String name, final ClaimableOperation claimableOperation, final AnalysedComponent analysedComponent, final ClassName className) {
        return delegate().createMethod(name, claimableOperation, analysedComponent, className);
    }

    /**
     * Creates or returns the builder for the constructor. There will only be one constructor for each generated class
     * @return The constructor builder.
     */
    default MethodSpec.Builder createConstructor() {
        return delegate().createConstructor();
    }
}

package io.github.cbarlin.aru.core.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.inference.Holder;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

/**
 * An enriched wrapper around a {@link RecordComponentElement}.
 * <p>
 * This wrapper includes things like links to the utils class of the parent record, if it
 *   is part of a constructor parameter or not, and tracks which claims have been made.
 * <p>
 * Child classes of this one exist as specialisations of this component. Generally speaking, they
 *   shouldn't add too many extra functions (as they can probably be re-used across e.g. optional dependencies)
 * <p>
 * To create a specialisation of this component, you'll need to implement an {@link io.github.cbarlin.aru.core.types.ComponentAnalyser}
 *   and it should determine if the specialisation should be built, and build it
 */
public class AnalysedComponent {
    protected final RecordComponentElement element;
    protected final AnalysedRecord parentRecord;
    protected final UtilsProcessingContext context;
    protected final boolean isIntendedConstructorParam;
    protected final TypeMirror componentType;
    protected final TypeName typeName;
    protected final List<AnalysedTypeConverter> analysedTypeConverters;

    protected final Map<ClaimableOperation, RecordVisitor> claimedOperations = new HashMap<>();
    protected Optional<ProcessingTarget> targetAnalysedType = Optional.empty();

    /**
     * Construction of the Analysed component. This construction does handle detecting
     *   {@link AnalysedTypeConverter} instances, but doesn't anything else.
     * 
     * Params should be self-explanatory
     */
    public AnalysedComponent(
        RecordComponentElement element, 
        AnalysedRecord parentRecord, 
        boolean isIntendedConstructorParam, 
        UtilsProcessingContext utilsProcessingContext
    ) {
        this.element = element;
        this.parentRecord = parentRecord;
        this.isIntendedConstructorParam = isIntendedConstructorParam;
        this.context = utilsProcessingContext;
        this.componentType = element.asType();
        if (componentType instanceof DeclaredType decl) {
            final TypeElement te = (TypeElement) decl.asElement();
            this.analysedTypeConverters = AnalysedTypeConverter.getTypeConverters(te);
            this.typeName = ClassName.get(te);
        } else {
            this.typeName = TypeName.get(componentType);
            this.analysedTypeConverters = List.of();
        }
    }

    /**
     * Inform this component that it's pointing at something else that's
     *   in-scope for processing (or was processed), and thus we can "bounce"
     *   into that record (e.g. fluent builder, pass on serialisations)
     */
    public void setAnalysedType(ProcessingTarget type) {
        this.targetAnalysedType = Optional.of(type);
    }

    /**
     * Check if something has been claimed. Useful if you are working on e.g.
     *  a Wither and want to ensure that a builder method exists
     */
    public final boolean isClaimed(final ClaimableOperation operation) {
        return claimedOperations.containsKey(operation);
    }

    /**
     * Attempt to claim an operation
     * @return true if claimed, or if the visitor already claimed the operation, and that it should continue processing
     */
    public final boolean attemptToClaim(RecordVisitor visitor) {
        if (OperationType.CLASS.equals(visitor.claimableOperation().operationType())) {
            // We only care about field ones here
            return true;
        }
        claimedOperations.putIfAbsent(visitor.claimableOperation(), visitor);
        return claimedOperations.get(visitor.claimableOperation()) == visitor;
    }

    /**
     * Retract a claim on an operation.
     * <p>
     * Retracting a claim that you do not hold will deliberately cause compilation to fail.
     * @param visitor The visitor retracting its claim
     */
    public final void retractClaim(RecordVisitor visitor) {
        if(OperationType.FIELD_AND_ACCESSORS.equals(visitor.claimableOperation().operationType())) {
            final RecordVisitor claimant = claimedOperations.get(visitor.claimableOperation());
            if (Objects.isNull(claimant) || visitor != claimant) {
                context.processingEnv().getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, "Internal Error: Visitor %s attempted to retract claim that it doesn't hold".formatted(visitor.getClass().getCanonicalName()), element);
            } else {
                claimedOperations.remove(visitor.claimableOperation());
            }
        }
    }

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
    public ToBeBuilt generationArtifact(final String generatedClassName, final String generatedWrapperClassName, final ClaimableOperation claimableOperation) {
        return parentRecord().utilsClassChildClass(generatedWrapperClassName, claimableOperation).childClassArtifact(generatedClassName, claimableOperation);
    }

     /**
     * Add a cross-reference with another {@link ProcessingTarget} that is relevant to the current one
     * <p>
     * Cross-references are for when one {@code *Utils} class calls something in another. It should <strong>not</strong> be
     *   confused with classes the target class references (although usually all of those will be referenced).
     */
    public void addCrossReference(ProcessingTarget other) {
        this.parentRecord().addCrossReference(other);
    }

    /**
     * Perform some operation within an "Unwrapped" version of the value
     * <p>
     * This can be used on items like {@code List<?>} or {@code Optional<?>} or even {@code Optional<List<?>>}
     * 
     * @param withUnwrappedName The name of the variable that this component has been unwrapped into
     * @param methodBuilder The builder that can be used to do the unwrapping
     */
    public final void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder) {
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
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName) {
        APContext.messager().printError("Attempt to invoke an unwrapping on a type that doesn't require it", element);
    }

    // Accessors below
    public AnalysedRecord parentRecord() {
        return this.parentRecord;
    }

    public RecordComponentElement element() {
        return element;
    }

    public String name() {
        return element.getSimpleName().toString();
    }

    public String nameFirstLetterCaps() {
        final String lcaseName = name();
        final String firstLetterCaps = StringUtils.toRootUpperCase(lcaseName.substring(0, 1));
        return firstLetterCaps + lcaseName.substring(1);
    }

    public ToBeBuilt builderArtifact() {
        return parentRecord.builderArtifact();
    }

    public UtilsProcessingContext context() {
        return context;
    }

    public ProcessingEnvironment processingEnv() {
        return context.processingEnv();
    }

    public AdvRecUtilsSettings settings() {
        return parentRecord.settings();
    }

    public boolean isIntendedConstructorParam() {
        return isIntendedConstructorParam;
    }

    public TypeMirror componentType() {
        return componentType;
    }

    public boolean isLoopable() {
        return false;
    }

    public boolean requiresUnwrapping() {
        return false;
    }

    /**
     * Un-nested type. For normal references, it will be the same as {@link #componentType()}
     * <p>
     * For lists, this will be the type within the list
     * <p>
     * For maps, this will be the "Value" of Map[Key, Value]
     */
    public TypeMirror unNestedPrimaryComponentType() {
        return componentType();
    }

    /**
     * Un-nested type. For normal references, it will be the same as {@link #componentType()}
     * <p>
     * For lists, this will be the type within the list (same as {@link #unNestedPrimaryComponentType()})
     * <p>
     * For maps, this will be the "Key" of Map[Key, Value]
     */
    public TypeMirror unNestedSecondaryComponentType() {
        return unNestedPrimaryComponentType();
    }

    public TypeName typeName() {
        return typeName;
    }

    /**
     * @see #unNestedPrimaryComponentType()
     */
    public TypeName unNestedPrimaryTypeName() {
        return typeName();
    }

    /**
     * @see #unNestedSecondaryComponentType()
     */
    public TypeName unNestedSecondaryTypeName() {
        return unNestedPrimaryTypeName();
    }

    public Optional<ClassName> className() {
        return targetAnalysedType
            .map(ProcessingTarget::typeElement)
            .map(ClassName::get);
    }

    public Optional<ProcessingTarget> targetAnalysedType() {
        return targetAnalysedType;
    }

    public Optional<ClassName> erasedWrapperTypeName() {
        return Optional.empty();
    }

    protected void setTargetAnalysedType(Optional<ProcessingTarget> targetAnalysedType) {
        this.targetAnalysedType = targetAnalysedType;
    }

    public List<AnalysedTypeConverter> converters() {
        return this.analysedTypeConverters;
    }

    @Override
    public String toString() {
        return className().map(ClassName::canonicalName)
            .orElse(typeName().toString());
    }

    private final Map<ClassName, Optional<?>> annotations = new HashMap<>();

    // Fine because the only population is the known-correct impl method
    @SuppressWarnings("unchecked")
    public <T> Optional<T> findPrism(ClassName annotationClassName, Class<T> prismClass) {
        return (Optional<T>) annotations.computeIfAbsent(annotationClassName, c -> findPrismImpl(c, prismClass));
    }

    private <T> Optional<T> findPrismImpl(ClassName annotationClassName, Class<T> prismClass) {
        return Holder.adaptors(annotationClassName).stream()
            .map(cn -> cn.optionalInstanceOn(element))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(prismClass::isInstance)
            .map(prismClass::cast)
            .findFirst()
            .or(
                () -> Holder.inferencers(annotationClassName).stream()
                        .map(inf -> inf.inferAnnotationMirror(element, context, parentRecord().prism()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(
                            (AnnotationMirror tm) -> Holder.adaptors(annotationClassName).stream()
                                    .map(cn -> cn.optionalInstanceOf(tm))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .filter(prismClass::isInstance)
                                    .map(prismClass::cast)
                        )
                        .findFirst()
            );
    }
}

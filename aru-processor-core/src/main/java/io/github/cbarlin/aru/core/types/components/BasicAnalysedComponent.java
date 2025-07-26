package io.github.cbarlin.aru.core.types.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.inference.Holder;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.types.OperationType;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
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
 * To create a specialisation of this component, you'll need to implement an {@link io.github.cbarlin.aru.core.analysers.ComponentAnalyser}
 *   and it should determine if the specialisation should be built, and build it
 */
public final class BasicAnalysedComponent implements AnalysedComponent {
    protected final RecordComponentElement element;
    protected final AnalysedRecord parentRecord;
    protected final UtilsProcessingContext context;
    protected final boolean isIntendedConstructorParam;
    protected final TypeMirror componentType;
    protected final TypeName typeName;
    protected final List<AnalysedTypeConverter> analysedTypeConverters;
    protected final Map<ClaimableOperation, RecordVisitor> claimedOperations = new HashMap<>();
    protected final Optional<ProcessingTarget> targetAnalysedType;

    /**
     * Construction of the Analysed component. This construction does handle detecting
     *   {@link AnalysedTypeConverter} instances, but doesn’t perform anything else.
     * 
     * Params should be self-explanatory
     */
    public BasicAnalysedComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord, 
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        this.element = element;
        this.parentRecord = parentRecord;
        this.isIntendedConstructorParam = isIntendedConstructorParam;
        this.context = utilsProcessingContext;
        this.componentType = element.asType();
        this.typeName = TypeName.get(componentType);
        if (componentType instanceof final DeclaredType decl) {
            final TypeElement te = (TypeElement) decl.asElement();
            this.analysedTypeConverters = AnalysedTypeConverter.getTypeConverters(te);
        } else {
            this.analysedTypeConverters = List.of();
        }
        this.targetAnalysedType = Optional.of(componentType)
            .map(APContext::asTypeElement)
            .map(utilsProcessingContext::analysedType);
    }

    @Override
    public final boolean isClaimed(final ClaimableOperation operation) {
        return claimedOperations.containsKey(operation);
    }

    @Override
    public final boolean attemptToClaim(final RecordVisitor visitor) {
        if (OperationType.CLASS.equals(visitor.claimableOperation().operationType())) {
            // We only care about field ones here
            return true;
        }
        claimedOperations.putIfAbsent(visitor.claimableOperation(), visitor);
        return claimedOperations.get(visitor.claimableOperation()) == visitor;
    }

    @Override
    public final void retractClaim(final RecordVisitor visitor) {
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

    @Override
    public ToBeBuilt generationArtifact(final String generatedClassName, final String generatedWrapperClassName, final ClaimableOperation claimableOperation) {
        return parentRecord().utilsClassChildClass(generatedWrapperClassName, claimableOperation).childClassArtifact(generatedClassName, claimableOperation);
    }

    @Override
    public void addCrossReference(final ProcessingTarget other) {
        this.parentRecord().addCrossReference(other);
    }

    @Override
    public final void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder) {
        withinUnwrapped(withUnwrappedName, methodBuilder, name());
    }

    @Override
    public final void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName) {
        withinUnwrapped(withUnwrappedName, methodBuilder, incomingName, unNestedPrimaryTypeName());
    }

    @Override
    public void withinUnwrapped(final Consumer<String> withUnwrappedName, final MethodSpec.Builder methodBuilder, final String incomingName, final TypeName unwrappedTypeName) {
        APContext.messager().printError("Attempt to invoke an unwrapping on a type that doesn't require it", element);
        throw new UnsupportedOperationException("Cannot unwrap a type that isn't wrapped");
    }

    // Accessors below
    @Override
    public AnalysedRecord parentRecord() {
        return this.parentRecord;
    }

    @Override
    public RecordComponentElement element() {
        return element;
    }

    @Override
    public String name() {
        return element.getSimpleName().toString();
    }

    @Override
    public boolean isIntendedConstructorParam() {
        return isIntendedConstructorParam;
    }

    @Override
    public TypeMirror componentType() {
        return componentType;
    }

    @Override
    public TypeName typeName() {
        return typeName;
    }

    @Override
    public Optional<ProcessingTarget> targetAnalysedType() {
        return targetAnalysedType;
    }

    @Override
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
    @Override
    public <T> Optional<T> findPrism(final ClassName annotationClassName, final Class<T> prismClass) {
        return (Optional<T>) annotations.computeIfAbsent(annotationClassName, c -> findPrismImpl(c, prismClass));
    }

    private <T> Optional<T> findPrismImpl(final ClassName annotationClassName, final Class<T> prismClass) {
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
                            (final AnnotationMirror tm) -> Holder.adaptors(annotationClassName).stream()
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

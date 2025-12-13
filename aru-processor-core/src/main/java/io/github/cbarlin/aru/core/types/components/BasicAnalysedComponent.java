package io.github.cbarlin.aru.core.types.components;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.types.OperationType;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An enriched wrapper around a {@link RecordComponentElement}.
 * <p>
 * This wrapper includes things like links to the utils class of the parent record, if it
 *   is part of a constructor parameter or not, and tracks which claims have been made.
 * <p>
 * Child classes of this one exist as specialisations of this component. Generally speaking, they
 *   shouldn't add too many extra functions (as they can probably be re-used across e.g. optional dependencies)
 * <p>
 */
public final class BasicAnalysedComponent implements AnalysedComponent {
    private final RecordComponentElement element;
    private final AnalysedRecord parentRecord;
    private final UtilsProcessingContext context;
    private final boolean isIntendedConstructorParam;
    private final TypeMirror componentType;
    private final TypeName typeName;
    private final Map<ClaimableOperation, RecordVisitor> claimedOperations = new HashMap<>();
    private final Optional<ProcessingTarget> targetAnalysedType;
    private final TypeName typeNameWithoutAnnotations;
    private final TypeName typeNameNullable;
    private final TypeName typeNameNonNullable;
    private final Optional<ClassName> className;

    /**
     * Construction of the Analysed component. This construction does handle detecting
     *   {@link AnalysedTypeConverter} instances, but doesn’t perform anything else.
     * <p>
     * Params should be self-explanatory
     */
    public BasicAnalysedComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord, 
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext,
        final Optional<ProcessingTarget> targetAnalysedType
    ) {
        this.element = element;
        this.parentRecord = parentRecord;
        this.isIntendedConstructorParam = isIntendedConstructorParam;
        this.context = utilsProcessingContext;
        this.componentType = element.asType();
        this.typeName = TypeName.get(componentType);
        this.targetAnalysedType = targetAnalysedType;
        this.typeNameWithoutAnnotations = this.typeName.withoutAnnotations();
        this.typeNameNullable = deepAnnotate(typeName, CommonsConstants.NULLABLE_ANNOTATION);
        this.typeNameNonNullable = deepAnnotate(typeName, CommonsConstants.NON_NULL_ANNOTATION);
        className = targetAnalysedType
                .map(ProcessingTarget::typeElement)
                .map(ClassName::get)
                .map(ClassName::withoutAnnotations);
    }

    private static TypeName deepAnnotate(final TypeName incoming, final AnnotationSpec toAdd) {
        if (incoming.isAnnotated()) {
            return deepAnnotate(incoming.withoutAnnotations(), toAdd);
        } else if (incoming.isPrimitive()) {
            return incoming;
        } else if (incoming instanceof final ParameterizedTypeName ptn) {
            final TypeName[] typeArguments = ptn.typeArguments.stream()
                .map(ta -> deepAnnotate(ta, toAdd))
                .toArray(TypeName[]::new);
            return ParameterizedTypeName.get(ptn.rawType.annotated(List.of(toAdd)), typeArguments);
        } else {
            return incoming.annotated(toAdd);
        }
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
    public Optional<ClassName> className() {
        return className;
    }

    @Override
    public Optional<ProcessingTarget> targetAnalysedType() {
        return targetAnalysedType;
    }

    @Override
    public String toString() {
        return className().map(ClassName::canonicalName)
            .orElse(typeName().toString());
    }

    @Override
    public TypeName typeNameWithoutAnnotations() {
        return typeNameWithoutAnnotations;
    }

    @Override
    public TypeName typeNameNullable() {
        return typeNameNullable;
    }

    @Override
    public TypeName typeNameNonNull() {
        return typeNameNonNullable;
    }
}

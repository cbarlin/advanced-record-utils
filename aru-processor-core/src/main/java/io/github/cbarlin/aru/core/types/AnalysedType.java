package io.github.cbarlin.aru.core.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.GENERATED_ANNOTATION;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import org.jspecify.annotations.NonNull;

import io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltClass;
import io.github.cbarlin.aru.core.inference.Holder;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeSpec;

public abstract sealed class AnalysedType implements ProcessingTarget permits AnalysedInterface, AnalysedRecord {
    
    protected final UtilsProcessingContext utilsProcessingContext;
    protected final TypeElement typeElement;
    protected final TypeMirror typeMirror;
    protected final ToBeBuilt utilsClass;
    protected final AdvRecUtilsSettings settings;

    protected final Set<ClassName> referencedUtilsClasses = new HashSet<>();
    protected final Map<ClaimableOperation, RecordVisitor> claimedOperations = new HashMap<>();
    /**
     * Annotations that are not on the Element but are "projected"
     *   into it that may be relevent (e.g. `NotNull` which is implied from a package annotation)
     */
    protected final Map<ClassName, List<AnnotationMirror>> projectedRelevantAnnotations = new HashMap<>();
    
    //#region Construction
    protected AnalysedType(final TypeElement element, final UtilsProcessingContext context, final AdvRecUtilsSettings parentSettings) {
        this.typeElement = element;
        this.typeMirror = element.asType();
        this.utilsProcessingContext = context;
        this.settings = finaliseSettings(parentSettings, element, context);
        this.utilsClass = new ToBeBuiltClass(createUtilsClassName(settings, element), context);
        utilsClass.builder()
            .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
            .addSuperinterface(Names.GENERATED_UTIL)
            .addAnnotation(
                AnnotationSpec.builder(GENERATED_ANNOTATION)
                    .addMember("value", "$S", AdvRecUtilsProcessor.class.getCanonicalName())
                    .build()
            );
        switch (settings().prism().logGeneration()) {
            case "SLF4J_UTILS_CLASS", "SLF4J_PER_SUBCLASS":
                utilsClass.addLogger();
                break;
            case "SLF4J_GENERATED_UTIL_INTERFACE":
                utilsClass.addLogger(Names.GENERATED_UTIL);
                break;
            default:
                break;
        }
    }

    private static ClassName createUtilsClassName(final AdvRecUtilsSettings settings, final TypeElement element) {
        final String originalClassName = element.getSimpleName().toString();
        final String targetPackage = AdvancedRecordUtilsPrism.getOptionalOn(element)
            .map(ignored -> ClassName.get(element).packageName())
            .orElseGet(() -> settings.packageName());
        final String utilsCn = settings.prism().typeNameOptions().utilsImplementationPrefix() + originalClassName + settings.prism().typeNameOptions().utilsImplementationSuffix();
        return ClassName.get(targetPackage, utilsCn);
    }

    private static AdvRecUtilsSettings finaliseSettings(final AdvRecUtilsSettings pAdvRecUtilsSettings, final TypeElement element, final UtilsProcessingContext utilsProcessingContext) {
        return AdvRecUtilsSettings.wrapOptional(element, utilsProcessingContext.processingEnv())
            .map(pref -> AdvRecUtilsSettings.merge(pref, pAdvRecUtilsSettings))
            .orElse(pAdvRecUtilsSettings);
    }
    //#endregion

    //#region Artifact management
    /**
     * Obtain an existing or create a new generation artifact.
     * <p>
     * Note: if creating a generation artiface, there must be something that places the `@Generated` annotation on it
     * @param generatedName The name to actually call the class when the source code is written out. Will automatically be a subclass of `Utils` so do not include the prefix
     * @param operation The operation creating the artifact
     * 
     * @return The artifact requested
     */

    @NonNull
    public ToBeBuilt utilsClassChildClass(final String generatedName, final ClaimableOperation operation) {
        return addLogger(utilsClass.childClassArtifact(generatedName, operation));
    }

    @NonNull
    public ToBeBuilt utilsClassChildInterface(final String generatedName, final ClaimableOperation operation) {
        return addLogger(utilsClass.childInterfaceArtifact(generatedName, operation));
    }

    private ToBeBuilt addLogger(final ToBeBuilt item) {
        if ("SLF4J_PER_SUBCLASS".equals(settings().prism().logGeneration())) {
            item.addLogger();
        }
        return item;
    }

    /**
     * Add a cross-reference with another {@link AnalysedType} that is relevant to the current one
     * <p>
     * Cross-references are for when one {@code *Utils} class calls something in another. It should <strong>not</strong> be
     *   confused with classes the target class references (although usually all of those will be referenced).
     */
    public void addCrossReference(final ProcessingTarget other) {
        if (this != other) {
            this.referencedUtilsClasses.add(other.utilsClassName());
        }
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
    public final boolean attemptToClaim(final RecordVisitor visitor) {
        if (!OperationType.CLASS.equals(visitor.claimableOperation().operationType())) {
            // We only track class-level claims
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
    public final void retractClaim(final RecordVisitor visitor) {
        if(OperationType.CLASS.equals(visitor.claimableOperation().operationType())) {
            final RecordVisitor claimant = claimedOperations.get(visitor.claimableOperation());
            if (Objects.isNull(claimant) || visitor != claimant) {
                processingEnv().getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, "Internal Error: Visitor %s attempted to retract claim that it doesn't hold".formatted(visitor.getClass().getCanonicalName()), typeElement);
            } else {
                claimedOperations.remove(visitor.claimableOperation());
            }
        }
    }
    //#endregion

    /**
     * The current processing environment
     */
    public ProcessingEnvironment processingEnv() {
        return utilsProcessingContext.processingEnv();
    }

    //#region Eq, HC, accessors, blah
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((typeMirror == null) ? 0 : typeMirror.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AnalysedType other = (AnalysedType) obj;
        if (typeMirror == null) {
            if (other.typeMirror != null)
                return false;
        } else if (!processingEnv().getTypeUtils().isSameType(typeMirror, other.typeMirror))
            return false;
        return true;
    }

    public String typeSimpleName() {
        return typeElement.getSimpleName().toString();
    }

    public UtilsProcessingContext utilsProcessingContext() {
        return utilsProcessingContext;
    }

    public ClassName className() {
        return ClassName.get(typeElement);
    }

    public TypeElement typeElement() {
        return typeElement;
    }

    public TypeMirror typeMirror() {
        return typeMirror;
    }

    public TypeSpec.Builder utilsClassBuilder() {
        return utilsClass.builder();
    }

    public ToBeBuilt utilsClass() {
        return utilsClass;
    }

    public AdvRecUtilsSettings settings() {
        return settings;
    }

    public AdvancedRecordUtilsPrism prism() {
        return settings().prism();
    }
    @Override
    public String toString() {
        return className().canonicalName();
    }
    //#endregion

    protected final Map<ClassName, Optional> annotations = new HashMap<>();

    public <T> Optional<T> findPrism(ClassName annotationClassName, Class<T> prismClass) {
        return (Optional<T>) annotations.computeIfAbsent(annotationClassName, c -> findPrismImpl(c, prismClass));
    }

    private <T> Optional<T> findPrismImpl(ClassName annotationClassName, Class<T> prismClass) {
        return Holder.adaptors().stream()
            .filter(cn -> annotationClassName.equals(cn.supportedAnnotationClassName()))
            .map(cn -> cn.optionalInstanceOn(typeElement))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(prismClass::isInstance)
            .map(prismClass::cast)
            .findFirst()
            .or(
                () -> Holder.inferencers().stream()
                        .filter(inf -> annotationClassName.equals(inf.supportedAnnotationClassName()))
                        .map(inf -> inf.inferAnnotationMirror(typeElement, utilsProcessingContext, prism()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(
                            (AnnotationMirror tm) -> Holder.adaptors().stream()
                                    .filter(cn -> annotationClassName.equals(cn.supportedAnnotationClassName()))
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

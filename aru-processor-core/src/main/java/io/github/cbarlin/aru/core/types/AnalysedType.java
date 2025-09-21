package io.github.cbarlin.aru.core.types;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtilsGenerated;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltClass;
import io.github.cbarlin.aru.core.mirrorhandlers.MergedMirror;
import io.github.cbarlin.aru.core.mirrorhandlers.MirrorOfDefaults;
import io.github.cbarlin.aru.core.mirrorhandlers.SourceTrackingAnnotationMirror;
import io.github.cbarlin.aru.core.mirrorhandlers.StackingAnnotationMirror;
import io.github.cbarlin.aru.core.mirrorhandlers.ToPrettierAnnotationSpec;
import io.github.cbarlin.aru.core.visitors.AruVisitor;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeSpec;
import org.apache.commons.lang3.Strings;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.github.cbarlin.aru.core.CommonsConstants.JDOC_PARA;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARU_GENERATED;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARU_INTERNAL_UTILS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARU_MAIN_ANNOTATION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARU_ROOT_ELEMENT_INFORMATION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARU_SETTINGS_SOURCE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARU_VERSION;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.GENERATED_ANNOTATION;

public abstract sealed class AnalysedType implements ProcessingTarget permits AnalysedInterface, AnalysedRecord {

    private static final String CLASS_REFERENCE_FORMAT = "$T.class";
    private static final MirrorOfDefaults DEFAULTS = new MirrorOfDefaults(
        OptionalClassDetector.optionalDependencyTypeElement(ARU_MAIN_ANNOTATION)
             .orElseThrow(() -> new IllegalStateException("Required annotation not found: " + ARU_MAIN_ANNOTATION))
    );

    protected final UtilsProcessingContext utilsProcessingContext;
    protected final TypeElement typeElement;
    protected final TypeMirror typeMirror;
    protected final ToBeBuilt utilsClass;
    protected final AdvRecUtilsSettings settings;

    protected final Set<ClassName> referencedUtilsClasses = new HashSet<>();
    protected final Set<ClassName> referencedTypeConverters = new HashSet<>();
    protected final Map<ClaimableOperation, AruVisitor<?>> claimedOperations = new HashMap<>();
    protected final Set<Element> referencingRootElements = new HashSet<>();
    protected final Set<Element> nonReferencingRootElements = new HashSet<>();
    protected final Set<TypeElement> knownMetaAnnotations = new HashSet<>();

    //#region Construction
    protected AnalysedType(final TypeElement element, final UtilsProcessingContext context, final AdvRecUtilsSettings settings) {
        this.typeElement = element;
        this.typeMirror = element.asType();
        this.utilsProcessingContext = context;
        this.settings = settings;
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
            .or(
                () -> Optional.of(ClassName.get(element).packageName())
                    .filter(str -> Strings.CS.startsWith(str, settings.packageName()))
            )
            .orElseGet(settings::packageName);
        final String utilsCn = settings.prism().typeNameOptions().utilsImplementationPrefix() + originalClassName + settings.prism().typeNameOptions().utilsImplementationSuffix();
        return ClassName.get(targetPackage, utilsCn);
    }

    //#endregion

    //#region Artifact management
    /**
     * Obtain an existing or create a new generation artifact.
     * <p>
     * Note: if creating a generation artifact, there must be something that places the `@Generated` annotation on it
     * @param generatedName The name to actually call the class when the source code is written out. Will automatically be a subclass of `Utils` so do not include the prefix
     * @param operation The operation creating the artifact
     * 
     * @return The artifact requested
     */

    public ToBeBuilt utilsClassChildClass(final String generatedName, final ClaimableOperation operation) {
        return addLogger(utilsClass.childClassArtifact(generatedName, operation));
    }

    public ToBeBuilt utilsClassChildInterface(final String generatedName, final ClaimableOperation operation) {
        return addLogger(utilsClass.childInterfaceArtifact(generatedName, operation));
    }

    private ToBeBuilt addLogger(final ToBeBuilt item) {
        if ("SLF4J_PER_SUBCLASS".equals(settings().prism().logGeneration())) {
            item.addLogger();
        }
        return item;
    }

    public void addRootElement(final Element rootElement, final boolean references) {
        if (rootElement == typeElement) {
            return;
        }
        if (references || (rootElement instanceof final PackageElement pkg && typeElement.getEnclosingElement().equals(pkg))) {
            referencingRootElements.add(rootElement);
            nonReferencingRootElements.remove(rootElement);
        } else if (!referencingRootElements.contains(rootElement)) {
            nonReferencingRootElements.add(rootElement);
        }
    }

    public void addKnownMetaAnnotation(final TypeElement typeElement) {
        this.knownMetaAnnotations.add(typeElement);
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
            if (other instanceof final AnalysedType at) {
                this.referencingRootElements.addAll(at.referencingRootElements);
            }
        }
    }

    /**
     * Add a reference to a {@link AnalysedTypeConverter} that is relevant to the current builder
     */
    public void addTypeConverter(final AnalysedTypeConverter analysedTypeConverter) {
        this.referencedTypeConverters.add(analysedTypeConverter.enclosedClass());
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
    public final boolean attemptToClaim(final AruVisitor<?> visitor) {
        if (!OperationType.CLASS.equals(visitor.claimableOperation().operationType())) {
            // We only track class-level claims
            return true;
        }
        claimedOperations.putIfAbsent(visitor.claimableOperation(), visitor);
        return claimedOperations.get(visitor.claimableOperation()).equals(visitor);
    }

    /**
     * Retract a claim on an operation.
     * <p>
     * Retracting a claim that you do not hold will deliberately cause compilation to fail.
     * @param visitor The visitor retracting its claim
     */
    public final void retractClaim(final AruVisitor<?> visitor) {
        if(OperationType.CLASS.equals(visitor.claimableOperation().operationType())) {
            final AruVisitor<?> claimant = claimedOperations.get(visitor.claimableOperation());
            if (Objects.isNull(claimant) || visitor != claimant) {
                APContext.messager()
                    .printMessage(Diagnostic.Kind.ERROR, "Internal Error: Visitor %s attempted to retract claim that it doesn't hold".formatted(visitor.getClass().getCanonicalName()), typeElement);
            } else {
                claimedOperations.remove(visitor.claimableOperation());
            }
        }
    }
    //#endregion

    public String typeSimpleName() {
        return typeElement.getSimpleName().toString();
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

    public void addFullGeneratedAnnotation() {
        final TypeSpec.Builder utilsBuilder = utilsClassBuilder();
        final AnnotationSpec versionAnnotation = AnnotationSpec.builder(ARU_VERSION)
            .addMember("major", "$L", AdvancedRecordUtilsGenerated.Version.MAJOR_VERSION)
            .addMember("minor", "$L", AdvancedRecordUtilsGenerated.Version.MINOR_VERSION)
            .addMember("patch", "$L", AdvancedRecordUtilsGenerated.Version.PATCH_VERSION)
            .build();
        final AnnotationSpec.Builder utilsGeneratorAnnotation = AnnotationSpec.builder(ARU_GENERATED)
            .addMember("generatedFor", CLASS_REFERENCE_FORMAT, className())
            .addMember("version", versionAnnotation);

        final AnnotationMirror userSettings = settings().prism().mirror;

        utilsGeneratorAnnotation.addMember("settings", ToPrettierAnnotationSpec.convertToAnnotationSpec(userSettings));

        final List<ToBeBuilt> childArtifacts = new ArrayList<>();
        utilsClass.visitChildArtifacts(childArtifacts::add);
        // Sort in ClassName order
        childArtifacts.sort(Comparator.comparing(ToBeBuilt::className));
        childArtifacts.forEach(toBeBuilt -> {
            final AnnotationSpec childArtifact = AnnotationSpec.builder(ARU_INTERNAL_UTILS)
               .addMember("type", "$S", toBeBuilt.className())
               .addMember("implementation", CLASS_REFERENCE_FORMAT, toBeBuilt.className())
               .build();
            utilsGeneratorAnnotation.addMember("internalUtils", childArtifact);
        });

        referencedUtilsClasses.stream()
            .sorted(Comparator.comparing(ClassName::canonicalName))
            .forEach(cn -> utilsGeneratorAnnotation.addMember("references", CLASS_REFERENCE_FORMAT, cn));

        referencedTypeConverters.stream()
            .sorted(Comparator.comparing(ClassName::canonicalName))
            .forEach(cn -> utilsGeneratorAnnotation.addMember("usedTypeConverters", CLASS_REFERENCE_FORMAT, cn));

        addRootElementInformation(utilsGeneratorAnnotation);
        knownMetaAnnotations.stream()
            .map(ClassName::get)
            .sorted(Comparator.comparing(ClassName::canonicalName))
            .forEach(cn -> utilsGeneratorAnnotation.addMember("knownMetaAnnotations", CLASS_REFERENCE_FORMAT, cn));

        addSettingSources(utilsGeneratorAnnotation);

        final AnnotationMirror withDefaults = new MergedMirror(userSettings, DEFAULTS);
        utilsGeneratorAnnotation.addMember("fullyComputedSettings", ToPrettierAnnotationSpec.convertToAnnotationSpec(withDefaults));

        utilsBuilder.addAnnotation(utilsGeneratorAnnotation.build())
            .addJavadoc("An auto-generated utility class to work with {@link $T} objects", className())
            .addJavadoc(JDOC_PARA)
            .addJavadoc("This includes a builder, as well as other generated utilities based on the values provided to the {@link $T} annotation", ARU_MAIN_ANNOTATION)
            .addJavadoc(JDOC_PARA);
        utilsBuilder.addJavadoc("For more details, see the GitHub page for cbarlin/advanced-record-utils");
    }

    private void addSettingSources(final AnnotationSpec.Builder utilsGeneratorAnnotation) {
        if (settings.prism().mirror instanceof final StackingAnnotationMirror sam) {
            Element previousElement = null;
            final List<SourceTrackingAnnotationMirror> mirrors = sam.trackingMirrors();
            for (int i = 0; i < mirrors.size(); i++) {
                final SourceTrackingAnnotationMirror stam = mirrors.get(i);
                if (Objects.equals(stam.annotatedElement(), previousElement)) {
                    continue;
                }
                final AnnotationSpec settings = ToPrettierAnnotationSpec.convertToAnnotationSpec(stam);
                final AnnotationSpec.Builder sourceDetails = AnnotationSpec.builder(ARU_SETTINGS_SOURCE)
                    .addMember("settings", settings)
                    .addMember("distance", "$L", i);
                switch (stam.annotatedElement()) {
                    case final TypeElement te -> sourceDetails.addMember("annotatedType", "$T.class", ClassName.get(te));
                    case final PackageElement pe -> sourceDetails.addMember("annotatedPackage", "$S", pe.getQualifiedName().toString());
                    case final ModuleElement me -> sourceDetails.addMember("annotatedModule", "$S", me.getQualifiedName().toString());
                    default -> {
                        continue;
                    }
                }
                utilsGeneratorAnnotation.addMember("settingSources", sourceDetails.build());
                previousElement = stam.annotatedElement();
            }
        }
    }

    private void addRootElementInformation(final AnnotationSpec.Builder utilsGeneratorAnnotation) {
        addTypeRootElementInformation(referencingRootElements, utilsGeneratorAnnotation, true);
        addPackageRootElementInformation(referencingRootElements, utilsGeneratorAnnotation, true);
        addTypeRootElementInformation(nonReferencingRootElements, utilsGeneratorAnnotation, false);
        addPackageRootElementInformation(nonReferencingRootElements, utilsGeneratorAnnotation, false);
        referencingRootElements.forEach(element -> utilsClass.builder().addOriginatingElement(element));
    }

    private void addPackageRootElementInformation(final Set<Element> elements, final AnnotationSpec.Builder generatorAnnotation, final boolean references) {
        elements.stream()
            .filter(PackageElement.class::isInstance)
            .map(PackageElement.class::cast)
            .map(PackageElement::getQualifiedName)
            .map(Name::toString)
            .sorted()
            .forEach((final String name) -> {
                final AnnotationSpec rootElementInformation = AnnotationSpec.builder(ARU_ROOT_ELEMENT_INFORMATION)
                    .addMember("rootPackage", "$S", name)
                    .addMember("referencesCurrentItem", "$L", String.valueOf(references))
                    .build();
                generatorAnnotation.addMember("rootElements", rootElementInformation);
            });
    }

    private void addTypeRootElementInformation(final Set<Element> elements, final AnnotationSpec.Builder generatorAnnotation, final boolean references) {
        elements.stream()
            .filter(TypeElement.class::isInstance)
            .map(TypeElement.class::cast)
            .map(ClassName::get)
            .sorted(Comparator.comparing(ClassName::canonicalName))
            .map(utilsProcessingContext::analysedType)
            .flatMap(Optional::stream)
            .forEach((final ProcessingTarget target) -> {
                final AnnotationSpec rootElementInformation = AnnotationSpec.builder(ARU_ROOT_ELEMENT_INFORMATION)
                    .addMember("rootType", CLASS_REFERENCE_FORMAT, ClassName.get(target.typeElement()))
                    .addMember("utilsClass", CLASS_REFERENCE_FORMAT, target.utilsClassName())
                    .addMember("referencesCurrentItem", "$L", String.valueOf(references))
                    .build();
                generatorAnnotation.addMember("rootElements", rootElementInformation);
            });
    }
}

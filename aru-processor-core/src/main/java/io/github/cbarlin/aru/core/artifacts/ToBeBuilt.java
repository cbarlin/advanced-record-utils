package io.github.cbarlin.aru.core.artifacts;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants.InternalReferenceNames;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.sorting.FieldSpecComparator;
import io.github.cbarlin.aru.core.artifacts.sorting.MethodSpecComparator;
import io.github.cbarlin.aru.core.artifacts.sorting.TypeSpecComparator;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedType;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeSpec;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract sealed class ToBeBuilt implements GenerationArtifact<ToBeBuilt>, IToBeBuilt<ToBeBuilt> permits ToBeBuiltClass, ToBeBuiltInterface, ToBeBuiltRecord {
    private static final String LOGGER_INITIALISER = "$T.getLogger($T.class)";
    // We have our two generated annotations, and sometimes the `NullMarked`/`NullUnmarked`
    private static final int GENERATED_ANNOTATIONS = 3;
    private final ClassName className;
    protected final TypeSpec.Builder classBuilder;
    private final UtilsProcessingContext utilsProcessingContext;
    private final Map<String, MethodSpec.Builder> unfinishedMethods = new HashMap<>();
    private final Map<String, ToBeBuilt> childArtifacts = new HashMap<>();
    private boolean loggerAdded = false;

    protected ToBeBuilt(final ClassName className, final TypeSpec.Builder typeBuilder, final UtilsProcessingContext utilsProcessingContext) {
        this.className = className;
        this.classBuilder = typeBuilder;
        this.utilsProcessingContext = utilsProcessingContext;
    }

    protected ToBeBuilt(final ClassName className, final UtilsProcessingContext utilsProcessingContext, final Function<ClassName, TypeSpec.Builder> builderBuilder) {
        this.className = className;
        this.classBuilder = builderBuilder.apply(className);
        this.utilsProcessingContext = utilsProcessingContext;
    }

    @Override
    public ToBeBuilt delegate() {
        return this;
    }

    @Override
    public boolean hasContent() {
        return (!unfinishedMethods.isEmpty()) || (!childArtifacts.isEmpty()) || (!classBuilder.methodSpecs.isEmpty()) || (!classBuilder.typeSpecs.isEmpty()) || (classBuilder.annotations.size() > GENERATED_ANNOTATIONS);
    }

    @Override
    public ClassName className() {
        return className;
    }

    @Override
    public TypeSpec finishClass() {
        if (classBuilder.annotations.isEmpty()) {
            utilsProcessingContext.processingEnv()
                .getMessager()
                .printMessage(Diagnostic.Kind.ERROR, "Internal error - attempted to generate class without generated annotation present - " + className.canonicalName());
        }

        // Sort the fields (kinda)
        Collections.sort(classBuilder.fieldSpecs, FieldSpecComparator.INSTANCE);
        // Add all the methods (and then sort them)
        for (final MethodSpec.Builder methodSpec : unfinishedMethods.values()) {
            Collections.sort(methodSpec.annotations, Comparator.comparingInt((final AnnotationSpec a) -> a.toString().length()));
        }
        unfinishedMethods.values().stream().map(MethodSpec.Builder::build).forEach(classBuilder::addMethod);
        Collections.sort(classBuilder.methodSpecs, MethodSpecComparator.INSTANCE);
        // And of course, the nested classes
        childArtifacts.values().stream().map(ToBeBuilt::finishClass).forEach(classBuilder::addType);
        Collections.sort(classBuilder.typeSpecs, TypeSpecComparator.INSTANCE);
        Collections.sort(classBuilder.annotations, Comparator.comparingInt((final AnnotationSpec a) -> a.toString().length()));
        try {
            return classBuilder.build();
        } catch (Exception e) {
            APContext.messager().printError("Oops!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cleanup() {
        // Make sure that all children are cleaned up too
        childArtifacts.values()
            .forEach(ToBeBuilt::cleanup);
        unfinishedMethods.clear();
        classBuilder.fieldSpecs.clear();
        classBuilder.methodSpecs.clear();
        classBuilder.typeSpecs.clear();
        classBuilder.annotations.clear();
    }

    @Override
    public TypeSpec.Builder builder() {
        return this.classBuilder;
    }

    @Override
    public ToBeBuilt addField(final FieldSpec fieldSpec) {
        final FieldSpec.Builder b = fieldSpec.toBuilder();
        Collections.sort(b.annotations, Comparator.comparingInt((final AnnotationSpec a) -> a.toString().length()));
        classBuilder.addField(b.build());
        return this;
    }

    @Override
    public ToBeBuilt addLogger() {
        return this.addLogger(className());
    }

    @Override
    public ToBeBuilt addLogger(final ClassName loggerReferenceClass) {
        if (!loggerAdded) {
            classBuilder.addField(
                FieldSpec.builder(Names.LOGGER, InternalReferenceNames.LOGGER_NAME, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer(LOGGER_INITIALISER, Names.LOGGER_FACTORY, loggerReferenceClass)
                    .build()
            );
            loggerAdded = true;
        }
        return this;
    }

    //#region child artifact

    @Override
    @NonNull
    public ToBeBuilt childRecordArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return childArtifacts.computeIfAbsent(generatedCodeName + "#" + claimableOperation.operationName(), ignored -> new ToBeBuiltRecord(className.nestedClass(generatedCodeName), utilsProcessingContext));
    }

    @Override
    @NonNull
    public ToBeBuilt childClassArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return childArtifacts.computeIfAbsent(generatedCodeName + "#" + claimableOperation.operationName(), ignored -> new ToBeBuiltClass(className.nestedClass(generatedCodeName), utilsProcessingContext));
    }

    @Override
    @NonNull
    public ToBeBuilt childInterfaceArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return childArtifacts.computeIfAbsent(generatedCodeName + "#" + claimableOperation.operationName(), ignored -> new ToBeBuiltInterface(className.nestedClass(generatedCodeName), utilsProcessingContext));
    }

    @Override
    public @Nullable ToBeBuilt childArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return childArtifacts.get(claimableOperation.operationName());
    }
    
    @Override
    public void visitChildArtifacts(final Consumer<ToBeBuilt> consumer) {
        this.childArtifacts.values().forEach(consumer);
    }
    //#endregion

    //#region method adders
    // Private method taking in the name for the map
    private MethodSpec.Builder createMethod(final String generatedCodeName, final String internallyReferencedName) {
        return unfinishedMethods.computeIfAbsent(generatedCodeName + "#" + internallyReferencedName, ignored -> MethodSpec.methodBuilder(generatedCodeName).addModifiers(Modifier.PUBLIC));
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return createMethod(generatedCodeName, claimableOperation.operationName());
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Modifier modifier) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "^" + modifier.name()).addModifiers(modifier);
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final AnalysedType targetAnalysedType) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "$$" + targetAnalysedType.className().canonicalName());
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final AnalysedTypeConverter analysedTypeConverter) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "$$" + analysedTypeConverter);
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element) {
        return createMethod(
            generatedCodeName, 
            claimableOperation.operationName() + "$$" + 
            element.getSimpleName().toString() + "$$" +
            extractTypeAsNameString(element)
        );
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element, final ClassName className) {
        return createMethod(
            generatedCodeName, 
            claimableOperation.operationName() + "$$" + 
            element.getSimpleName().toString() + "$$" +
            extractTypeAsNameString(element) + "$$$" +
            className.canonicalName()
        );
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element, final ParameterizedTypeName ptn) {
        return createMethod(
            generatedCodeName, 
            claimableOperation.operationName() + "$$" + 
            element.getSimpleName().toString() + "$$" +
            extractTypeAsNameString(element) + "$$$" +
            ptn.toString()
        );
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final ClassName className) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "$$$" + className.simpleName());
    }

    @Override
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final ParameterizedTypeName paramTn) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "$#$" + paramTn.toString());
    }

    @Override
    public MethodSpec.Builder createMethod(final String name, final ClaimableOperation claimableOperation, final AnalysedComponent analysedComponent) {
        return createMethod(name, claimableOperation, analysedComponent.element());
    }

    @Override
    public MethodSpec.Builder createMethod(final String name, final ClaimableOperation claimableOperation, final AnalysedComponent analysedComponent, final ClassName className) {
        return createMethod(name, claimableOperation, analysedComponent.element(), className);
    }

    @Override
    public MethodSpec.Builder createConstructor() {
        return unfinishedMethods.computeIfAbsent("<init>", ignored -> MethodSpec.constructorBuilder());
    }
    //#endregion

    private static String extractTypeAsNameString(final Element element) {
        return switch (element) {
            case final VariableElement ve -> ve.getSimpleName().toString() + "ve";
            case final ExecutableElement ee -> APContext.types().asElement(ee.getReturnType()).getSimpleName().toString() + "ee";
            case final TypeElement te -> "tehe" + te.getSimpleName().toString();
            case final ModuleElement me -> "hi how did you get here?" + me.getSimpleName().toString();
            case final PackageElement pe -> "No seriously how did you get here?" + pe.getSimpleName().toString();
            case final TypeParameterElement tpe -> "tpe" + tpe.getSimpleName().toString() + "this isn't consistent";
            case null, default -> "UNKNOWN";
        };
    }
    
}

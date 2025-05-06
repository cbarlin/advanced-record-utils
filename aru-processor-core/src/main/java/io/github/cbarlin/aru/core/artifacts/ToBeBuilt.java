package io.github.cbarlin.aru.core.artifacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.CommonsConstants.InternalReferenceNames;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedType;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeSpec;

public abstract class ToBeBuilt implements GenerationArtifact<ToBeBuilt> {
    private static final String LOGGER_INITIALISER = "$T.getLogger($T.class)";
    // We have our two generated annotations, and sometimes the `NullMarked`/`NullUnmarked`
    private static final int GENERATED_ANNOTATIONS = 3;
    private final ClassName className;
    private final TypeSpec.Builder classBuilder;
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

    /**
     * Determine if this artifact actually has any content
     */
    public boolean hasContent() {
        return (!unfinishedMethods.isEmpty()) || (!childArtifacts.isEmpty()) || (!classBuilder.methodSpecs.isEmpty()) || (!classBuilder.typeSpecs.isEmpty()) || (classBuilder.annotations.size() > GENERATED_ANNOTATIONS);
    }

    public ClassName className() {
        return className;
    }

    public TypeSpec finishClass() {
        if (classBuilder.annotations.isEmpty()) {
            utilsProcessingContext.processingEnv()
                .getMessager()
                .printMessage(Diagnostic.Kind.ERROR, "Internal error - attempted to generate class without generated annotation present - " + className.canonicalName());
        }
        // Let's finish the class!
        final List<String> methodNames = new ArrayList<>(unfinishedMethods.keySet());
        Collections.sort(methodNames);
        for(final String methodName : methodNames) {
            final MethodSpec.Builder methodBuilder = unfinishedMethods.get(methodName);
            final MethodSpec method = methodBuilder.build();
            if (methodBuilder.annotations.isEmpty()) {
                utilsProcessingContext.processingEnv()
                    .getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, "Internal error - attempted to generate method without generated annotation present - " + className.canonicalName() + "#" + method.name);
            }
            
            classBuilder.addMethod(method);
        }
        // And of course, the nested classes
        final List<String> typeNames = new ArrayList<>(childArtifacts.keySet());
        Collections.sort(typeNames);
        for (final String typeName : typeNames) {
            final ToBeBuilt child = childArtifacts.get(typeName);
            final TypeSpec nested = child.finishClass();
            classBuilder.addType(nested);
        }
        return classBuilder.build();
    }

    public TypeSpec.Builder builder() {
        return this.classBuilder;
    }

    public void addField(final FieldSpec fieldSpec) {
        classBuilder.addField(fieldSpec);
    }

    /**
     * Add a logger to the class using own class name. Calling multiple times will have no effect beyond the first.
     * <p>
     * Note: Callers should check if loggers should be added to this class
     */
    public void addLogger() {
        this.addLogger(className());
    }

    /**
     * Add a logger to the class, using the provided class name. Calling multiple times will have no effect beyond the first.
     * <p>
     * Note: Callers should check if loggers should be added to this class
     */
    public void addLogger(final ClassName loggerReferenceClass) {
        if (!loggerAdded) {
            classBuilder.addField(
                FieldSpec.builder(Names.LOGGER, InternalReferenceNames.LOGGER_NAME, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer(LOGGER_INITIALISER, Names.LOGGER_FACTORY, loggerReferenceClass)
                    .build()
            );
            loggerAdded = true;
        }
    }

    //#region child artifact
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
    public ToBeBuilt childClassArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return childArtifacts.computeIfAbsent(generatedCodeName + "#" + claimableOperation.operationName(), ignored -> new ToBeBuiltClass(className.nestedClass(generatedCodeName), utilsProcessingContext));
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
    public ToBeBuilt childInterfaceArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return childArtifacts.computeIfAbsent(generatedCodeName + "#" + claimableOperation.operationName(), ignored -> new ToBeBuiltInterface(className.nestedClass(generatedCodeName), utilsProcessingContext));
    }

    /**
     * Obtain a child artifact of any kind
     */
    @Override
    public @Nullable ToBeBuilt childArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return childArtifacts.get(claimableOperation.operationName());
    }
    /**
     * Visit all the child artifacts
     */
    public void visitChildArtifacts(final Consumer<ToBeBuilt> consumer) {
        this.childArtifacts.values().forEach(consumer);
    }
    //#endregion

    //#region method adders
    // Private method taking in the name for the map
    private MethodSpec.Builder createMethod(final String generatedCodeName, final String internallyReferencedName) {
        return unfinishedMethods.computeIfAbsent(generatedCodeName + "#" + internallyReferencedName, ignored -> MethodSpec.methodBuilder(generatedCodeName).addModifiers(Modifier.PUBLIC));
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
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return createMethod(generatedCodeName, claimableOperation.operationName());
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
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Modifier modifier) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "^" + modifier.name()).addModifiers(modifier);
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link AnalysedType}
     * <p>
     * E.g. <code>createMethod("addBook", "adderFluent", bookAnalysedType)</code>
     * 
     * @see #createMethod(String, String)
     */
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final AnalysedType targetAnalysedType) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "$$" + targetAnalysedType.className().canonicalName());
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link Element}
     * <p>
     * E.g. <code>createMethod("addBook", "adderSingle", bookElement)</code>
     * @see #createMethod(String, String)
     */
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element) {
        return createMethod(
            generatedCodeName, 
            claimableOperation.operationName() + "$$" + 
            element.getSimpleName().toString() + "$$" +
            extractTypeAsNameString(element)
        );
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link Element} and ClassName
     * <p>
     * E.g. <code>createMethod("addBook", "adderSingle", bookElement)</code>
     * @see #createMethod(String, String)
     */
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element, final ClassName className) {
        return createMethod(
            generatedCodeName, 
            claimableOperation.operationName() + "$$" + 
            element.getSimpleName().toString() + "$$" +
            extractTypeAsNameString(element) + "$$$" +
            className.canonicalName()
        );
    }


    /**
     * Creates a public method on the generated class that's made for a specific {@link Element} and ClassName
     * <p>
     * E.g. <code>createMethod("addBook", "adderSingle", bookElement)</code>
     * @see #createMethod(String, String)
     */
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final Element element, final ParameterizedTypeName ptn) {
        return createMethod(
            generatedCodeName, 
            claimableOperation.operationName() + "$$" + 
            element.getSimpleName().toString() + "$$" +
            extractTypeAsNameString(element) + "$$$" +
            ptn.toString()
        );
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link ClassName}
     * <p>
     * E.g. <code>createMethod("addBook", "adderAllIterable", bookClassName)</code>
     * @see #createMethod(String, String)
     */
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final ClassName className) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "$$$" + className.simpleName());
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link ClassName}
     * <p>
     * E.g. <code>createMethod("addBook", "adderAllIterable", bookClassName)</code>
     * @see #createMethod(String, String)
     */
    public MethodSpec.Builder createMethod(final String generatedCodeName, final ClaimableOperation claimableOperation, final ParameterizedTypeName paramTn) {
        return createMethod(generatedCodeName, claimableOperation.operationName() + "$#$" + paramTn.toString());
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link AnalysedComponent}
     * <p>
     * E.g. <code>createMethod("addBook", "adderAllIterable", analysedComponent)</code>
     * @see #createMethod(String, String)
     */
    public MethodSpec.Builder createMethod(final String name, final ClaimableOperation claimableOperation, final AnalysedComponent analysedComponent) {
        return createMethod(name, claimableOperation, analysedComponent.element());
    }

    /**
     * Creates a public method on the generated class that's made for a specific {@link AnalysedComponent} and {@link ClassName}
     * <p>
     * E.g. <code>createMethod("addBook", "adderAllIterable", analysedComponent)</code>
     * @see #createMethod(String, String)
     */
    public MethodSpec.Builder createMethod(final String name, final ClaimableOperation claimableOperation, final AnalysedComponent analysedComponent, final ClassName className) {
        return createMethod(name, claimableOperation, analysedComponent.element(), className);
    }

    /**
     * Creates or returns the builder for the constructor. There will only be one constructor for each generated class
     * @return The constructor builder.
     */
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

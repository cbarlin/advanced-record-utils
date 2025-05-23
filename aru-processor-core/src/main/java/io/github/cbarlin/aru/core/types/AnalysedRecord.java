package io.github.cbarlin.aru.core.types;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.XML_ELEMENTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.prism.prison.JsonSubTypesPrism;
import io.github.cbarlin.aru.prism.prison.JsonSubTypesPrism.TypePrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

public final class AnalysedRecord extends AnalysedType {
    
    private static TypeElement useInterfaceDefaultClass;
    private static final List<ComponentAnalyser> COMPONENT_ANALYSERS;

    static {
        final List<ComponentAnalyser> sortable = new ArrayList<>();
        ServiceLoader.load(ComponentAnalyser.class, ComponentAnalyser.class.getClassLoader())
            .iterator()
            .forEachRemaining(sortable::add);
        Collections.sort(sortable, (a, b) -> b.compareTo(a));
        COMPONENT_ANALYSERS = List.copyOf(sortable);
    }

    private final List<AnalysedComponent> components = new ArrayList<>();
    private final List<ExecutableElement> nonConstructorAccessorMethods = new ArrayList<>();
    private final ExecutableElement canonicalConstructor;
    private final ExecutableElement intendedConstructor;
    private final TypeElement intendedTypeElement;

    //#region Analysis

    // Do not use `computeIfAbsent` because we may end up adding to it elsewhere
    @SuppressWarnings({"java:S3824"})
    public static ProcessingTarget analyse(final Map<TypeElement, ProcessingTarget> analysed, 
                                       final TypeElement element, 
                                       final UtilsProcessingContext context, 
                                       final AdvRecUtilsSettings parentSettings
    ) {
        context.checkForAndAddExistingUtils(element);
        if (!analysed.containsKey(element)) {
            final AnalysedRecord self = new AnalysedRecord(element, context, parentSettings);
            analysed.put(element, self);
            analyseImpl(analysed, self);
        }
        return (ProcessingTarget) analysed.get(element);
    }

    private static void analyseImpl(final Map<TypeElement, ProcessingTarget> analysed, final AnalysedRecord analysisTarget) {
        final Set<Name> intendedParams = analysisTarget.intendedConstructor.getParameters().stream()
                .map(VariableElement::getSimpleName)
                .collect(Collectors.toSet());
        final Set<ExecutableElement> ignoredExecutableElements = new HashSet<>();
        for (final RecordComponentElement componentElement : analysisTarget.typeElement.getRecordComponents()) {
            analyseRecordComponentElement(analysed, analysisTarget, intendedParams, ignoredExecutableElements, componentElement);
        }
        final List<? extends Element> enclosedElements = analysisTarget.typeElement.getEnclosedElements();
        ignoredExecutableElements.addAll(ElementFilter.constructorsIn(enclosedElements));

        analysisTarget.nonConstructorAccessorMethods.addAll(
            ElementFilter.methodsIn(enclosedElements).stream().filter(Predicate.not(ignoredExecutableElements::contains)).toList()
        );
    }

    private static AnalysedComponent getComponent(
        final RecordComponentElement element, 
        final AnalysedRecord parentRecord, 
        final boolean isIntendedConstructorParam, 
        final UtilsProcessingContext utilsProcessingContext
    ) {
        for (final ComponentAnalyser componentAnalyser : COMPONENT_ANALYSERS) {
            final var result = componentAnalyser.analyse(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
            if (result.isPresent()) {
                return result.get();
            }
        }
        APContext.messager().printError("Cannot find an analyser for element", element);
        // Return the basic type
        return new AnalysedComponent(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
    }

    private static void analyseRecordComponentElement(final Map<TypeElement, ProcessingTarget> analysed, 
                                                      final AnalysedRecord analysisTarget, 
                                                      final Set<Name> intendedParams, 
                                                      final Set<ExecutableElement> ignoredExecutableElements,            
                                                      final RecordComponentElement componentElement
    ) {
        final boolean isIntended = intendedParams.contains(componentElement.getSimpleName());
        final var env = analysisTarget.processingEnv();
        final AnalysedComponent analysedComponent = getComponent(componentElement, analysisTarget, isIntended, analysisTarget.utilsProcessingContext());
        // Determine if we need to analyse this target too
        if (analysedComponent.proceedDownTree()) {
            final TypeMirror tmTypeOfComponent = analysedComponent.unNestedPrimaryComponentType();
            final ProcessingTarget target = extractReference(analysed, analysisTarget, env, tmTypeOfComponent);
            if (Objects.nonNull(target) && target instanceof ProcessingTarget analysedTypeTarget) {
                analysedComponent.setAnalysedType(analysedTypeTarget);
                // Extract any `XmlElements` from the component
                if (target instanceof final AnalysedInterface analysedInterface) {
                    checkForImplAnnotations(analysed, analysisTarget, componentElement, env, analysedInterface);
                }
            }
        }
        analysisTarget.components.add(analysedComponent);
        ignoredExecutableElements.add(componentElement.getAccessor());
    }

    private static void checkForImplAnnotations(final Map<TypeElement, ProcessingTarget> analysed, 
                                                final AnalysedRecord analysisTarget,
                                                final RecordComponentElement componentElement, 
                                                final ProcessingEnvironment env,
                                                final AnalysedInterface analysedInterface
    ) {
        checkForXmlImplAnnotations(analysed, analysisTarget, env, analysedInterface);
        checkForJacksonImplAnnotations(analysed, analysisTarget, componentElement, env, analysedInterface);
    }

    private static void checkForXmlImplAnnotations(final Map<TypeElement, ProcessingTarget> analysed, 
                                                   final AnalysedRecord analysisTarget,
                                                   final ProcessingEnvironment env, 
                                                   final AnalysedInterface analysedInterface
    ) {
        final Optional<XmlElementsPrism> xmlElements = analysisTarget.findPrism(XML_ELEMENTS, XmlElementsPrism.class);
        if (xmlElements.isPresent()) {
            final XmlElementsPrism prism = xmlElements.get();
            for (final XmlElementPrism value : prism.value()) {
                final ProcessingTarget prismPull = extractReference(analysed, analysisTarget, env, value.type());
                if (Objects.nonNull(prismPull)) {
                    analysedInterface.addImplementingType(prismPull);
                }
            }
        }
    }

    private static void checkForJacksonImplAnnotations(final Map<TypeElement, ProcessingTarget> analysed, 
                                                       final AnalysedRecord analysisTarget,
                                                       final RecordComponentElement componentElement, 
                                                       final ProcessingEnvironment env,
                                                       final AnalysedInterface analysedInterface
    ) {
        if (JsonSubTypesPrism.isPresent(componentElement)) {
            final JsonSubTypesPrism prism = JsonSubTypesPrism.getInstanceOn(componentElement);
            for (final TypePrism value : prism.value()) {
                final ProcessingTarget prismPull = extractReference(analysed, analysisTarget, env, value.value());
                if (Objects.nonNull(prismPull)) {
                    analysedInterface.addImplementingType(prismPull);
                }
            }
        }
        if (JsonSubTypesPrism.isPresent(componentElement.getAccessor())) {
            final JsonSubTypesPrism prism = JsonSubTypesPrism.getInstanceOn(componentElement.getAccessor());
            for (final TypePrism value : prism.value()) {
                final ProcessingTarget prismPull = extractReference(analysed, analysisTarget, env, value.value());
                if (Objects.nonNull(prismPull)) {
                    analysedInterface.addImplementingType(prismPull);
                }
            }
        }
    }

    @Nullable
    private static ProcessingTarget extractReference(final Map<TypeElement, ProcessingTarget> analysed, 
                                                 final AnalysedRecord analysisTarget, 
                                                 final ProcessingEnvironment env, 
                                                 final TypeMirror tmTypeOfComponent
    ) {
        if (Objects.nonNull(tmTypeOfComponent) && tmTypeOfComponent.getKind().equals(TypeKind.DECLARED)) {
            final TypeElement teTypeOfComponent = (TypeElement) env.getTypeUtils().asElement(tmTypeOfComponent);
            if (ElementKind.INTERFACE.equals(teTypeOfComponent.getKind())) {
                return AnalysedInterface.analyse(analysed, teTypeOfComponent, analysisTarget.utilsProcessingContext(), analysisTarget.settings());
            } else if (ElementKind.RECORD.equals(teTypeOfComponent.getKind())) {
                return AnalysedRecord.analyse(analysed, teTypeOfComponent, analysisTarget.utilsProcessingContext(), analysisTarget.settings());
            }
        }
        return null;
    }

    private static ExecutableElement findCanonicalConstructor(final TypeElement element, final UtilsProcessingContext context) {
        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        final List<ExecutableElement> constructors = ElementFilter.constructorsIn(enclosedElements);
        final ExecutableElement canonicalConstructor = constructors.stream()
            .filter(ctor -> matchesRecordComponents(ctor, element.getRecordComponents(), context))
            .findFirst()
            .orElse(null);

        if (Objects.isNull(canonicalConstructor)) {
            context.processingEnv().getMessager()
                .printMessage(Diagnostic.Kind.ERROR, "Cannot find canonical constructor", element);
        }
        return canonicalConstructor;
    }

    // Helper to check if ctor params match record components
    private static boolean matchesRecordComponents(final ExecutableElement ctor, final List<? extends RecordComponentElement> recordComponents, final UtilsProcessingContext context) {
        final List<? extends VariableElement> params = ctor.getParameters();
        if (params.size() != recordComponents.size()) return false;
        for (int i = 0; i < params.size(); i++) {
            if (!context.processingEnv().getTypeUtils().isSameType(params.get(i).asType(), recordComponents.get(i).asType())) {
                return false;
            }
        }
        return true;
    }

    private static ExecutableElement findIntendedConstructor(final TypeElement element, final UtilsProcessingContext context, final ExecutableElement canonicalConstructor) {
        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        final List<ExecutableElement> constructors = ElementFilter.constructorsIn(enclosedElements);
        if (constructors.size() <= 1) {
            // No other constructors to check!
            return canonicalConstructor;
        }
        // First, let's see if there's one that has out "TargetConstructor" annotation
        for(final ExecutableElement constructor : constructors) {
            if (Objects.nonNull(constructor.getAnnotation(AdvancedRecordUtils.TargetConstructor.class))) {
                if (!isSubsetConstructor(constructor, canonicalConstructor, context)) {
                    context.processingEnv().getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "Annotated constructor is not a subset of the canonical one", constructor);
                } else {
                    return constructor;
                }
            }
        }
        // Only continue searching if there are two options otherwise it'd be too confusing
        if (constructors.size() == 2) {
            if (isSubsetConstructor(constructors.get(0), canonicalConstructor, context)) {
                return constructors.get(0);
            }
            if(isSubsetConstructor(constructors.get(1), canonicalConstructor, context)) {
                return constructors.get(1);
            }
        }
        return canonicalConstructor;
    }

    private static boolean isSubsetConstructor(final ExecutableElement potentialConstructor, final ExecutableElement canonicalConstructor, final UtilsProcessingContext context) {
        if (potentialConstructor.getParameters().size() >= canonicalConstructor.getParameters().size()) {
            return false;
        }

        final List<? extends VariableElement> params1 = potentialConstructor.getParameters();
        final List<? extends VariableElement> params2 = canonicalConstructor.getParameters();
        final Set<TypeMirror> params2Types = params2.stream().map(VariableElement::asType).collect(Collectors.toSet());
        for (final VariableElement p1 : params1) {
            boolean foundMatch = false;
            for (final TypeMirror p2Type : params2Types) {
                if (context.processingEnv().getTypeUtils().isSameType(p1.asType(), p2Type)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                return false;
            }
        }
        // It's probably fine
        return true;
    }

    //#endregion

    //#region constructor
    public AnalysedRecord(final TypeElement element, final UtilsProcessingContext context, final AdvRecUtilsSettings parentSettings) {
        super(element, context, parentSettings);
        this.canonicalConstructor = findCanonicalConstructor(element, context);
        this.intendedConstructor = findIntendedConstructor(element, context, this.canonicalConstructor);
        if (
            element.equals(parentSettings.originalElement()) && 
            Objects.nonNull(settings().prism().useInterface()) &&
            (!processingEnv().getTypeUtils().isSameType(defaultClassUseInterface(processingEnv()), settings.prism().useInterface()))
        ) {
            intendedTypeElement = APContext.asTypeElement(settings().prism().useInterface());
        } else {
            intendedTypeElement = element;
        }
    }

    private static TypeMirror defaultClassUseInterface(ProcessingEnvironment processingEnvironment) {
        if (Objects.isNull(useInterfaceDefaultClass)) {
            useInterfaceDefaultClass = processingEnvironment.getElementUtils().getTypeElement("io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DEFAULT");
            Objects.requireNonNull(useInterfaceDefaultClass);
        }
        return useInterfaceDefaultClass.asType();
    }
    //#endregion

    //#region Getters etc

    public ToBeBuilt builderArtifact() {
        return utilsClassChildClass(settings.builderClassName(), CommonsConstants.Claims.CORE_BUILDER_CLASS);
    }

    public ClassName intendedType() {
        return ClassName.get(intendedTypeElement);
    }

    public ExecutableElement intendedConstructor() {
        return this.intendedConstructor;
    }

    public List<AnalysedComponent> components() {
        return List.copyOf(components);
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    //#endregion
}

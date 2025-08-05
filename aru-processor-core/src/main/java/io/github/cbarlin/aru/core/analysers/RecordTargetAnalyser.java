package io.github.cbarlin.aru.core.analysers;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.PreviousCompilationChecker;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.UseInterfaceDefaultClass;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.TargetConstructorPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementPrism;
import io.github.cbarlin.aru.prism.prison.XmlElementsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.TYPE_ALIAS;

@Component
@CoreGlobalScope
@Priority(2)
public final class RecordTargetAnalyser extends ConcreteTargetAnalyser {

    public RecordTargetAnalyser(final UtilsProcessingContext context, final UseInterfaceDefaultClass useInterfaceDefaultClass, final PreviousCompilationChecker previousCompilationChecker) {
        super(context, useInterfaceDefaultClass, previousCompilationChecker);
    }

    @Override
    public TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings) {
        if (!(ElementKind.RECORD.equals(element.getKind()) && element instanceof final TypeElement typeElement)) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        if (OptionalClassDetector.checkSameOrSubType(TypeName.get(element.asType()), TYPE_ALIAS)) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        final Optional<AdvRecUtilsSettings> presentSettingsOptional = AdvRecUtilsSettings.wrapOptional(typeElement, context.processingEnv());
        final AdvRecUtilsSettings settings = TargetAnalyser.finaliseSettings(presentSettingsOptional, parentSettings);
        if (Objects.isNull(settings)) {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "Was asked to process a record but there are no settings for it", typeElement);
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        // Let's get some needed properties!
        return processResult(typeElement, settings, presentSettingsOptional.isPresent());
    }

    private TargetAnalysisResult processResult(
        final TypeElement typeElement,
        final AdvRecUtilsSettings settings,
        final boolean isRoot
    ) {
        final TypeElement intendedType = intendedType(typeElement, settings);
        final ExecutableElement canonicalConstructor = findCanonicalConstructor(typeElement);
        final ExecutableElement intendedConstructor = findIntendedConstructor(typeElement, canonicalConstructor);
        final Optional<ProcessingTarget> result = Optional.of(new AnalysedRecord(typeElement, context, settings, intendedType, canonicalConstructor, intendedConstructor));
        final Set<TypeElement> references = findReferences(typeElement, settings);

        return new TargetAnalysisResult(result, references, isRoot, Set.of());
    }

    private Set<TypeElement> findReferences(final TypeElement typeElement, final AdvRecUtilsSettings settings) {
        final Set<TypeElement> references = new HashSet<>();

        final String packageName = ClassName.get(typeElement).packageName();

        for(final RecordComponentElement recordComponentElement : typeElement.getRecordComponents()) {
            processRecordElement(recordComponentElement, packageName, references, Boolean.TRUE.equals(settings.prism().attemptToFindExistingUtils()));
        }

        for (final TypeMirror anInterface : typeElement.getInterfaces()) {
            if (!TypeKind.ERROR.equals(anInterface.getKind())) {
                OptionalClassDetector.optionalDependencyTypeElement(TypeName.get(anInterface))
                    .ifPresent(references::add);
            }
        }

        AdvancedRecordUtilsPrism.getOptionalOn(typeElement)
                .map(AdvancedRecordUtilsPrism::importTargets)
                .orElse(List.of())
                .stream()
                .map(APContext.types()::asElement)
                .filter(Objects::nonNull)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .forEach(references::add);
        references.removeAll(recursiveIgnored(settings.prism()));

        return references;
    }

    private void processRecordElement(final RecordComponentElement recordComponentElement, final String packageName, final Set<TypeElement> references, final boolean attemptToFindLibrary) {
        OptionalClassDetector.optionalDependencyTypeElement(recordComponentElement.asType());
        final TypeMirror target = recordComponentElement.asType();
        final TypeName name = TypeName.get(target);
        checkAndAddToPotentialTargets(name, packageName, references, attemptToFindLibrary);

        XmlElementsPrism.getOptionalOn(recordComponentElement)
            .ifPresent(prism -> {
                for (final XmlElementPrism value : prism.value()) {
                    final TypeName tn = TypeName.get(value.type());
                    checkAndAddToPotentialTargets(tn, packageName, references, attemptToFindLibrary);
                }
            });

        XmlElementsPrism.getOptionalOn(recordComponentElement.getAccessor())
            .ifPresent(prism -> {
                for (final XmlElementPrism value : prism.value()) {
                    final TypeName tn = TypeName.get(value.type());
                    checkAndAddToPotentialTargets(tn, packageName, references, attemptToFindLibrary);
                }
            });
    }

    private TypeElement intendedType(final TypeElement targetElement, final AdvRecUtilsSettings settings) {
        if (
            targetElement.equals(settings.originalElement()) &&
            Objects.nonNull(settings.prism().useInterface()) &&
            (!context.processingEnv().getTypeUtils().isSameType(useInterfaceDefaultClass.mirror(), settings.prism().useInterface()))
        ) {
            return Objects.requireNonNullElse(APContext.asTypeElement(settings.prism().useInterface()), targetElement);
        }
        return targetElement;
    }

    private ExecutableElement findCanonicalConstructor(final TypeElement element) {
        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        final List<ExecutableElement> constructors = ElementFilter.constructorsIn(enclosedElements);
        final List<? extends RecordComponentElement> recordComponents = element.getRecordComponents();
        return constructors.stream()
            .filter(ctor -> matchesRecordComponents(ctor, recordComponents))
            .findFirst()
            .orElseThrow();

    }

    // Helper to check if ctor params match record components
    private boolean matchesRecordComponents(final ExecutableElement ctor, final List<? extends RecordComponentElement> recordComponents) {
        final List<? extends VariableElement> params = ctor.getParameters();
        if (params.size() != recordComponents.size()) return false;
        for (int i = 0; i < params.size(); i++) {
            if (!APContext.types().isSameType(params.get(i).asType(), recordComponents.get(i).asType())) {
                return false;
            }
        }
        return true;
    }

    private ExecutableElement findIntendedConstructor(final TypeElement element, final ExecutableElement canonicalConstructor) {
        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        final List<ExecutableElement> constructors = ElementFilter.constructorsIn(enclosedElements);
        if (constructors.size() <= 1) {
            // No other constructors to check!
            return canonicalConstructor;
        }
        // First, let's see if there's one that has our "TargetConstructor" annotation
        for(final ExecutableElement constructor : constructors) {
            if (TargetConstructorPrism.isPresent(constructor)) {
                if (!isSubsetConstructor(constructor, canonicalConstructor)) {
                    context.processingEnv().getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "Annotated constructor is not a subset of the canonical one", constructor);
                } else {
                    return constructor;
                }
            }
        }
        // Only continue searching if there are two options otherwise it'd be too confusing
        if (constructors.size() == 2) {
            if (isSubsetConstructor(constructors.get(0), canonicalConstructor)) {
                return constructors.get(0);
            }
            if(isSubsetConstructor(constructors.get(1), canonicalConstructor)) {
                return constructors.get(1);
            }
        }
        return canonicalConstructor;
    }

    private boolean isSubsetConstructor(final ExecutableElement potentialConstructor, final ExecutableElement canonicalConstructor) {
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

}

package io.github.cbarlin.aru.core.analysers;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.PreviousCompilationChecker;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.UseInterfaceDefaultClass;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.JsonBSubTypePrism;
import io.github.cbarlin.aru.prism.prison.JsonBSubTypesPrism;
import io.github.cbarlin.aru.prism.prison.JsonSubTypesPrism;
import io.github.cbarlin.aru.prism.prison.JsonSubTypesPrism.TypePrism;
import io.github.cbarlin.aru.prism.prison.XmlSeeAlsoPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@CoreGlobalScope
@Priority(4)
public final class InterfaceTargetAnalyser extends ConcreteTargetAnalyser {

    public InterfaceTargetAnalyser(final UtilsProcessingContext context, final UseInterfaceDefaultClass useInterfaceDefaultClass, final PreviousCompilationChecker previousCompilationChecker) {
        super(context, useInterfaceDefaultClass, previousCompilationChecker);
    }

    @Override
    public TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings) {
        if (!(element instanceof final TypeElement typeElement)) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        if (!ElementKind.INTERFACE.equals(typeElement.getKind())) {
            return TargetAnalysisResult.EMPTY_RESULT;
        }
        final Optional<AdvRecUtilsSettings> presentSettingsOptional = AdvRecUtilsSettings.wrapOptional(typeElement, context.processingEnv());
        final AdvRecUtilsSettings settings = TargetAnalyser.finaliseSettings(presentSettingsOptional, parentSettings);
        if (Objects.isNull(settings)) {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "Was asked to process an interface but there are no settings for it", typeElement);
            return TargetAnalysisResult.EMPTY_RESULT;
        }

        final AnalysedInterface analysedInterface = new AnalysedInterface(typeElement, context, settings);
        final Optional<ProcessingTarget> target = Optional.of(analysedInterface);
        final Set<TypeElement> found = new HashSet<>();
        findImplementations(typeElement, found, Boolean.TRUE.equals(settings.prism().attemptToFindExistingUtils()));
        found.forEach(analysedInterface::addUnprocessedImplementingType);
        final Set<TypeElement> references = new HashSet<>(analysedInterface.unprocessedImplementations());

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

        return new TargetAnalysisResult(target, references, false, Set.of());
    }



    private void findImplementations(final TypeElement typeElement, final Set<TypeElement> found, final boolean attemptToFindLibrary) {
        final String packageName = ClassName.get(typeElement).packageName();
        for (final TypeMirror target : typeElement.getPermittedSubclasses()) {
            checkAndAddToPotentialTargets(TypeName.get(target), packageName, found, attemptToFindLibrary);
        }

        if (XmlSeeAlsoPrism.isPresent(typeElement)) {
            final XmlSeeAlsoPrism prism = XmlSeeAlsoPrism.getInstanceOn(typeElement);
            for (final TypeMirror target : prism.value()) {
                checkAndAddToPotentialTargets(TypeName.get(target), packageName, found, attemptToFindLibrary);
            }
        }
        if (JsonBSubTypesPrism.isPresent(typeElement)) {
            final JsonBSubTypesPrism prism = JsonBSubTypesPrism.getInstanceOn(typeElement);
            for (final JsonBSubTypePrism target : prism.value()) {
                checkAndAddToPotentialTargets(TypeName.get(target.type()), packageName, found, attemptToFindLibrary);
            }
        }

        if (JsonSubTypesPrism.isPresent(typeElement)) {
            final JsonSubTypesPrism prism = JsonSubTypesPrism.getInstanceOn(typeElement);
            for (final TypePrism target : prism.value()) {
                checkAndAddToPotentialTargets(TypeName.get(target.value()), packageName, found, attemptToFindLibrary);
            }
        }
    }

}

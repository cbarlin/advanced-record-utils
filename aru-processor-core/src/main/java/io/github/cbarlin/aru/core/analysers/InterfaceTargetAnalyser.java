package io.github.cbarlin.aru.core.analysers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.PreviousCompilationChecker;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.types.UseInterfaceDefaultClass;
import io.github.cbarlin.aru.core.wiring.AruGlobal;
import io.github.cbarlin.aru.prism.prison.JsonBSubTypePrism;
import io.github.cbarlin.aru.prism.prison.JsonBSubTypesPrism;
import io.github.cbarlin.aru.prism.prison.JsonSubTypesPrism;
import io.github.cbarlin.aru.prism.prison.JsonSubTypesPrism.TypePrism;
import io.github.cbarlin.aru.prism.prison.XmlSeeAlsoPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

@Component
@AruGlobal
@Priority(4)
public final class InterfaceTargetAnalyser extends ConcreteTargetAnalyser {

    public InterfaceTargetAnalyser(final UtilsProcessingContext context, final UseInterfaceDefaultClass useInterfaceDefaultClass, final PreviousCompilationChecker previousCompilationChecker) {
        super(context, useInterfaceDefaultClass, previousCompilationChecker);
    }

    @Override
    public TargetAnalysisResult analyse(final Element element, final Optional<AdvRecUtilsSettings> parentSettings) {
        if ((!(element instanceof final TypeElement typeElement)) || (ElementKind.INTERFACE.equals(typeElement.getKind()))) {
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
        findImplementations(typeElement, found);
        found.forEach(analysedInterface::addUnprocessedImplementingType);
        return new TargetAnalysisResult(target, analysedInterface.unprocessedImplementations(), false);
    }

    private void findImplementations(final TypeElement typeElement, final Set<TypeElement> found) {
        final String packageName = ClassName.get(typeElement).packageName();
        for (final TypeMirror target : typeElement.getPermittedSubclasses()) {
            checkAndAddToPotentialTargets(TypeName.get(target), packageName, found);
        }

        if (XmlSeeAlsoPrism.isPresent(typeElement)) {
            final XmlSeeAlsoPrism prism = XmlSeeAlsoPrism.getInstanceOn(typeElement);
            for (final TypeMirror target : prism.value()) {
                checkAndAddToPotentialTargets(TypeName.get(target), packageName, found);
            }
        }
        if (JsonBSubTypesPrism.isPresent(typeElement)) {
            final JsonBSubTypesPrism prism = JsonBSubTypesPrism.getInstanceOn(typeElement);
            for (final JsonBSubTypePrism target : prism.value()) {
                checkAndAddToPotentialTargets(TypeName.get(target.type()), packageName, found);
            }
        }

        if (JsonSubTypesPrism.isPresent(typeElement)) {
            final JsonSubTypesPrism prism = JsonSubTypesPrism.getInstanceOn(typeElement);
            for (final TypePrism target : prism.value()) {
                checkAndAddToPotentialTargets(TypeName.get(target.value()), packageName, found);
            }
        }
    }

}

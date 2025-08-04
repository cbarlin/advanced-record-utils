package io.github.cbarlin.aru.core.analysers;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.PreviousCompilationChecker;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.mirrorhandlers.MapBasedAnnotationMirror;
import io.github.cbarlin.aru.core.types.UseInterfaceDefaultClass;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsGeneratedPrism;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.ArrayTypeName;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract sealed class ConcreteTargetAnalyser implements TargetAnalyser permits RecordTargetAnalyser, InterfaceTargetAnalyser {
    protected final UtilsProcessingContext context;
    protected final UseInterfaceDefaultClass useInterfaceDefaultClass;
    protected final PreviousCompilationChecker previousCompilationChecker;

    protected ConcreteTargetAnalyser(final UtilsProcessingContext context, final UseInterfaceDefaultClass useInterfaceDefaultClass, final PreviousCompilationChecker previousCompilationChecker) {
        this.context = context;
        this.useInterfaceDefaultClass = useInterfaceDefaultClass;
        this.previousCompilationChecker = previousCompilationChecker;
    }

    protected static Set<TypeElement> recursiveIgnored(final AdvancedRecordUtilsPrism settings) {
        return settings.recursiveExcluded()
                .stream()
                .map(APContext.types()::asElement)
                .filter(Objects::nonNull)
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .collect(Collectors.toUnmodifiableSet());
    }

    protected final void checkAndAddToPotentialTargets(final TypeName name, final String packageName, final Set<TypeElement> references, boolean attemptToFindLibrary) {
        switch (name) {
            case final ClassName className -> checkAndAddToPotentialTargets(packageName, className, references, attemptToFindLibrary);
            case ParameterizedTypeName ptn -> {
                for (final TypeName tn : ptn.typeArguments) {
                    if (tn instanceof final ClassName cn) {
                        checkAndAddToPotentialTargets(packageName, cn, references, attemptToFindLibrary);
                    }
                }
            }
            case final ArrayTypeName atn when atn.componentType instanceof final ClassName cname ->
                    checkAndAddToPotentialTargets(packageName, cname, references, attemptToFindLibrary);
            case null, default -> {
            }
        }
    }

    private void checkAndAddToPotentialTargets(final String packageName, final ClassName possibleTarget, final Set<TypeElement> references, boolean attemptToFindLibrary) {
        final Optional<TypeElement> element = OptionalClassDetector.optionalDependencyTypeElement(possibleTarget)
                .filter(Predicate.not(references::contains));
        element.ifPresent(possible -> {
            if (AdvancedRecordUtilsGeneratedPrism.isPresent(possible)) {
                // Odd... but OK
                references.add(possible);
            }
            AdvRecUtilsSettings.wrapOptional(possible, context.processingEnv())
                    .filter(ignored -> attemptToFindLibrary)
                    .flatMap(this::findPossibleUtilsClass)
                    .or(
                            // Attempt to find based on original settings
                            () -> Optional.of(possible)
                                    .filter(ignored -> attemptToFindLibrary)
                                    .map(ty -> (AnnotationMirror) new MapBasedAnnotationMirror(CommonsConstants.Names.ARU_MAIN_ANNOTATION))
                                    .map(am -> new AdvRecUtilsSettings(am, possible))
                                    .flatMap(this::findPossibleUtilsClass)
                    )
                    .or(
                            () -> Optional.of(possible)
                                    .filter(p -> possibleTarget.packageName().startsWith(packageName))
                                    .filter(p -> ElementKind.RECORD.equals(possible.getKind()) || ElementKind.INTERFACE.equals(possible.getKind()))
                    )
                    .ifPresent(references::add);
        });
    }

    private Optional<TypeElement> findPossibleUtilsClass(final AdvRecUtilsSettings settings) {
        final Element originaElement = settings.originalElement();
        Element packageEl = originaElement;
        while(!(Objects.isNull(packageEl) || packageEl instanceof PackageElement)) {
            packageEl = packageEl.getEnclosingElement();
        }
        if (Objects.isNull(packageEl) || (!(packageEl instanceof final PackageElement pack))) {
            return Optional.empty();
        }
        final String utilsClassName = settings.prism().typeNameOptions().utilsImplementationPrefix() + originaElement.getSimpleName().toString() + settings.prism().typeNameOptions().utilsImplementationSuffix();
        final String packageName = pack.getQualifiedName().toString();
        return recurseFindPlace(packageName, utilsClassName);
    }

    private Optional<TypeElement> recurseFindPlace(final String packageName, final String utilsClassName) {
        if (packageName.isBlank()) {
            return Optional.empty();
        }
        final ClassName check = ClassName.get(packageName, utilsClassName);
        final int lastDotIndex = packageName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return previousCompilationChecker.findTypeElement(check)
                .or(() -> recurseFindPlace(packageName.substring(0, lastDotIndex), utilsClassName));
        } else {
            return previousCompilationChecker.findTypeElement(check);
        }
    }
}

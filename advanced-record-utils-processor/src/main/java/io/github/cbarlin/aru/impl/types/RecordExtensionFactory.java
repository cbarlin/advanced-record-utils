package io.github.cbarlin.aru.impl.types;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.wiring.BasePerRecordScope;
import io.github.cbarlin.aru.prism.prison.BeforeBuildPrism;
import io.github.cbarlin.aru.prism.prison.BuilderExtensionPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Produce items that denote extensions to an individual record
 */
@Factory
@BasePerRecordScope
public final class RecordExtensionFactory {

    private final AnalysedRecord analysedRecord;

    public RecordExtensionFactory(final AnalysedRecord analysedRecord){
        this.analysedRecord = analysedRecord;
    }

    /**
     * Produce {@link RecordWithBeforeBuild} instances if there is an item in the current record
     *   with a valid {@link io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BeforeBuild} annotation present.
     * <p>
     * Attempts to accumulate as many errors as possible to return to the user, so this method is a bit bigger than it otherwise
     *   could be. However, since that means that the user would know everything that's wrong all at once, it's a better end-user experience
     * @return A wrapper around the executable element, if it exists and is valid.
     * @see io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BeforeBuild
     */
    @Bean
    @BeanTypes(RecordWithBeforeBuild.class)
    public Optional<RecordWithBeforeBuild> recordWithBeforeBuild() {
        for (final ExecutableElement executableElement : ElementFilter.methodsIn(analysedRecord.typeElement().getEnclosedElements())) {
            if (BeforeBuildPrism.isPresent(executableElement)) {
                // OK, now to validate it!
                // Accumulate as many errors as we can to report back to the user... so if they make multiple mistakes we can tell them
                if (!executableElement.getModifiers().contains(Modifier.STATIC)) {
                    APContext.messager().printMessage(Diagnostic.Kind.ERROR, "BeforeBuild annotations can only be placed on static methods", executableElement);
                }
                if (executableElement.getParameters().size() != 1) {
                    APContext.messager().printMessage(Diagnostic.Kind.ERROR, "BeforeBuild methods must only have one argument, which is the builder", executableElement);
                }
                if (!TypeName.get(executableElement.getReturnType()).equals(TypeName.VOID)) {
                    APContext.messager().printMessage(Diagnostic.Kind.ERROR, "BeforeBuild methods must be a void method", executableElement);
                    return Optional.empty();
                }
                // So we can safely grab the first element to check it is the builder
                if (executableElement.getParameters().isEmpty()) {
                    return Optional.empty();
                }
                final VariableElement param = executableElement.getParameters().getFirst();
                // Note: We cannot check the type normally because we are building the type! So string comparison it is...
                final String paramType = param.asType().toString();
                if (!(paramType.contains(analysedRecord.builderArtifact().className().simpleName()))) {
                    APContext.messager().printMessage(Diagnostic.Kind.ERROR, "BeforeBuild methods must have the builder the parameter");
                } else {
                    return Optional.of(new RecordWithBeforeBuild(executableElement));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Produce {@link RecordWithExtensions} instances if there are items in the current record
     *   with a valid {@link io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderExtension} annotation present.
     * <p>
     * Attempts to accumulate as many errors as possible to return to the user, so this method is a bit bigger than it otherwise
     *   could be. However, since that means that the user would know everything that's wrong all at once, it's a better end-user experience.
     * @return A wrapper around the executable elements, if any valid ones exist
     * @see io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderExtension
     */
    @Bean
    @BeanTypes(RecordWithExtensions.class)
    public Optional<RecordWithExtensions> recordWithExtensions() {
        final List<ExtensionMethod> elements = new ArrayList<>();
        for (final ExecutableElement executableElement : ElementFilter.methodsIn(analysedRecord.typeElement().getEnclosedElements())) {
            if (BuilderExtensionPrism.isPresent(executableElement)) {
                // OK, now to validate it!
                final @Nullable Boolean valid = establishValidity(executableElement);
                if (Boolean.TRUE.equals(valid)) {
                    final BuilderExtensionPrism prism = BuilderExtensionPrism.getInstanceOn(executableElement);
                    final Optional<ClassName> cn = Optional.ofNullable(prism)
                        .map(BuilderExtensionPrism::fromInterface)
                        .flatMap(OptionalClassDetector::optionalDependencyTypeElement)
                        .map(ClassName::get)
                        .filter(Predicate.not(CommonsConstants.Names.ARU_DEFAULT::equals));
                    elements.add(new ExtensionMethod(executableElement, cn));
                }
            }
        }
        return Optional.of(List.copyOf(elements))
            // It's only relevant if there's actual items.
            .filter(Predicate.not(List::isEmpty))
            .map(RecordWithExtensions::new);
    }

    @Nullable
    private Boolean establishValidity(final ExecutableElement executableElement) {
        boolean valid = true;
        if (!executableElement.getModifiers().contains(Modifier.STATIC)) {
            valid = false;
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "BuilderExtension annotations can only be placed on static methods", executableElement);
        }
        if (executableElement.getParameters().size() != 2) {
            valid = false;
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "BuilderExtension methods must only have two arguments, the first of which must be the builder", executableElement);
        }
        // So we can safely validate the first argument
        if (executableElement.getParameters().isEmpty()) {
            return null;
        }
        final VariableElement param = executableElement.getParameters().getFirst();
        // Note: We cannot check the type normally because we are building the type! So string comparison it is...
        final String paramType = param.asType().toString();
        if (!(paramType.contains(analysedRecord.builderArtifact().className().simpleName()))) {
            valid = false;
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "BuilderExtension methods must have the builder as the first parameter");
        }
        return valid;
    }
}

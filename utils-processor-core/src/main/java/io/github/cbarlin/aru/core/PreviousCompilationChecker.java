package io.github.cbarlin.aru.core;

import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.github.cbarlin.aru.annotations.GeneratedUtil;

import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * Utility to check if an element came from a previous compilation run
 */
public class PreviousCompilationChecker {

    private final Elements elementUtils;
    private final Types typeUtils;
    private final TypeElement generatedUtilElement;

    public PreviousCompilationChecker(final ProcessingEnvironment env) {
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        generatedUtilElement = elementUtils.getTypeElement(GeneratedUtil.class.getCanonicalName());
    }

    public Optional<TypeElement> findTypeElement(final ClassName className) {
        return Optional.ofNullable(elementUtils.getTypeElement(className.canonicalName()))
            .filter(te -> {
                return typeUtils.isSubtype(te.asType(), generatedUtilElement.asType());
            });
    }

    public Optional<TypeElement> loadGeneratedArtifact(final ClassName className) {
        return findTypeElement(className)
            .filter(AdvancedRecordUtilsGeneratedPrism::isPresent);
    }
}

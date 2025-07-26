package io.github.cbarlin.aru.core;

import io.avaje.inject.Component;
import io.avaje.inject.External;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.GENERATED_UTIL;

/**
 * Utility to check if an element came from a previous compilation run
 */
@Component
@CoreGlobalScope
public final class PreviousCompilationChecker {

    public PreviousCompilationChecker(final @External ProcessingEnvironment env) {
    }

    public Optional<TypeElement> findTypeElement(final ClassName className) {
        return OptionalClassDetector.optionalDependencyTypeElement(className)
                .filter(ign -> OptionalClassDetector.checkSameOrSubType(className, GENERATED_UTIL));
    }
}

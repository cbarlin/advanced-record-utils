package io.github.cbarlin.aru.impl.types;

import io.micronaut.sourcegen.javapoet.ClassName;

import javax.lang.model.element.ExecutableElement;
import java.util.Optional;

public record ExtensionMethod (
    ExecutableElement method,
    Optional<ClassName> fromInterface
) {
}

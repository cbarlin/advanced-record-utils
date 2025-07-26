package io.github.cbarlin.aru.core.factories;

import javax.lang.model.element.TypeElement;
import java.util.HashSet;

public record SupportedAnnotations (
    HashSet<TypeElement> annotations
) {

}

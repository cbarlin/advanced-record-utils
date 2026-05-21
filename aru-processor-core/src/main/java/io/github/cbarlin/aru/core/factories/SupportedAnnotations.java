package io.github.cbarlin.aru.core.factories;

import javax.lang.model.element.TypeElement;
import java.util.TreeSet;

public record SupportedAnnotations (
    TreeSet<TypeElement> annotations
) {

}

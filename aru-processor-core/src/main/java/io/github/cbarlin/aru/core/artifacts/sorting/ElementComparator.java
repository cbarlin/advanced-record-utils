package io.github.cbarlin.aru.core.artifacts.sorting;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import java.util.Comparator;

public final class ElementComparator {

    public static final Comparator<Element> INSTANCE = createComparator();

    private static Comparator<Element> createComparator() {
        return Comparator.comparingInt(ElementComparator::elementTypeToNumber)
                .thenComparing(e -> (e instanceof final QualifiedNameable qualifiedNameable) ? qualifiedNameable.getQualifiedName().toString() : e.getSimpleName().toString());
    }

    private static int elementTypeToNumber(final Element el) {
        return switch (el) {
            case final ModuleElement _ -> 1;
            case final PackageElement _ -> 2;
            case final ExecutableElement _ -> 3;
            case final TypeElement t -> 4 + t.getKind().ordinal();
            default -> 9999;
        };
    }
}

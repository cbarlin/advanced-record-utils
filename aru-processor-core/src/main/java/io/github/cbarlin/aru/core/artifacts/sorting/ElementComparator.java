package io.github.cbarlin.aru.core.artifacts.sorting;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import java.util.Comparator;
/**
 * Comparator for {`@link` Element} instances that provides deterministic ordering
 * to ensure reproducible builds. Elements are ordered by type hierarchy (modules,
 * packages, executables, then types), with ties broken by qualified or simple name.
 */
public final class ElementComparator {

    /**
     * The element comparator. Orders elements by type hierarchy with ties broken by name.
     *
     * @see ElementComparator
     */
    public static final Comparator<Element> INSTANCE = createComparator();

    /**
     * Creates the element comparator. First orders by element type rank,
     * then by qualified name (when available) or simple name as a tiebreaker.
     */
    private static Comparator<Element> createComparator() {
        return Comparator.comparingInt(ElementComparator::elementTypeToNumber)
                .thenComparing(ElementComparator::elementName);
    }

    private static String elementName(final Element element) {
        if (element instanceof final QualifiedNameable qualifiedNameable) {
            return qualifiedNameable.getQualifiedName().toString();
        }
        return element.getSimpleName().toString();
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

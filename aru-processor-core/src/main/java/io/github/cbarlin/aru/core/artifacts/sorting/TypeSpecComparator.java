package io.github.cbarlin.aru.core.artifacts.sorting;

import java.util.Comparator;

import javax.lang.model.element.Modifier;

import io.micronaut.sourcegen.javapoet.TypeSpec;

/**
 * A comparator for sorting {@link TypeSpec} instances based on a defined convention.
 * The sorting order is:
 * <ol>
 * <li>Visibility (public, protected, package-private, private)</li>
 * <li>Types that don't start with "_"</li>
 * <li>Interfaces before classes</li>
 * <li>Order by name</li>
 * </ol>
 */
public final class TypeSpecComparator {

    public static final Comparator<TypeSpec> INSTANCE = createComparator();

    private TypeSpecComparator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final Comparator<TypeSpec> createComparator() {
        return Comparator
            // 1. Visibility sort
            .comparingInt(TypeSpecComparator::getVisibilityScore)
            // 2. Types that don't start with "_"
            .thenComparing(t -> t.name.startsWith("_"))
            // 3. Interfaces before classes
            .thenComparing(t -> TypeSpec.Kind.INTERFACE.equals(t.kind))
            // 4. Sort by name
            .thenComparing(t -> t.name);
    }

    private static int getVisibilityScore(final TypeSpec typeSpec) {
        if (typeSpec.hasModifier(Modifier.PUBLIC)) {
            return 0;
        } else if (typeSpec.hasModifier(Modifier.PROTECTED)) {
            return 1;
        } else if (typeSpec.hasModifier(Modifier.PRIVATE)) {
            return 3;
        } else {
            return 2; // package-private
        }
    }
}

package io.github.cbarlin.aru.core.artifacts.sorting;

import java.util.Comparator;

import javax.lang.model.element.Modifier;

import io.micronaut.sourcegen.javapoet.MethodSpec;

/**
 * A comparator for sorting {@link MethodSpec} instances based on a defined convention.
 * The sorting order is:
 * <ol>
 * <li>Constructors</li>
 * <li>Visibility (public, protected, package-private, private)</li>
 * <li>Static vs. instance methods (varies by visibility):
 * <ul>
 * <li>For {@code public} and {@code protected} methods, {@code static} comes first.</li>
 * <li>For package-private and {@code private} methods, {@code static} comes last.</li>
 * </ul>
 * </li>
 * <li>Other modifiers (final, abstract, default)</li>
 * <li>Method name (alphabetical)</li>
 * <li>Number of parameters (ascending)</li>
 * <li>Full method body (as a fallback)</li>
 * </ol>
 */
public final class MethodSpecComparator {

    /**
     * Singleton instance of the comparator
     */
    public static final Comparator<MethodSpec> INSTANCE = createComparator();

    private MethodSpecComparator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Comparator<MethodSpec> createComparator() {
        return Comparator
            // 1. Constructors always come first
            .comparing((final MethodSpec m) -> !m.isConstructor())
            // 2. Then, sort by visibility score
            .thenComparingInt(MethodSpecComparator::getVisibilityScore)
            // 3. Within each visibility group, sort by static rank
            .thenComparingInt(MethodSpecComparator::getStaticScore)
            // 4. Then, sort by other important modifiers
            .thenComparing(m -> !m.hasModifier(Modifier.FINAL))
            .thenComparing(m -> !m.hasModifier(Modifier.ABSTRACT))
            .thenComparing(m -> !m.hasModifier(Modifier.DEFAULT))
            // 5. Then, sort by method name alphabetically
            .thenComparing(m -> m.name)
            // 6. For methods with the same name, sort by parameter count (fewer first)
            .thenComparingInt(m -> m.parameters.size())
            // 7. As a final tie-breaker, sort by the full method body
            .thenComparing(Object::toString);
    }

    private static int getVisibilityScore(final MethodSpec method) {
        if (method.hasModifier(Modifier.PUBLIC)) {
            return 0;
        } else if (method.hasModifier(Modifier.PROTECTED)) {
            return 1;
        } else if (method.hasModifier(Modifier.PRIVATE)) {
            return 3;
        } else {
            return 2; // package-private
        }
    }

    private static int getStaticScore(final MethodSpec method) {
        final boolean isStatic = method.hasModifier(Modifier.STATIC);
        final boolean isPublic = method.hasModifier(Modifier.PUBLIC);
        final boolean isProtected = method.hasModifier(Modifier.PROTECTED);

        if (isPublic || isProtected) {
            // For public/protected, static methods come first (rank 0).
            return isStatic ? 0 : 1;
        } else {
            // For private/package-private, instance methods come first (rank 0).
            return isStatic ? 1 : 0;
        }
    }
}
package io.github.cbarlin.aru.core.artifacts.sorting;

import java.util.Comparator;

import javax.lang.model.element.Modifier;

import io.micronaut.sourcegen.javapoet.FieldSpec;

/**
 * A comparator for sorting {@link FieldSpec} instances based on a defined convention.
 * The sorting order is:
 * <ol>
 * <li>log/logger first</li>
 * <li>Visibility (public, protected, package-private, private)</li>
 * <li>Static vs. instance fields</li>
 * <li>Final first</li>
 * <li>Field name (alphabetical)</li>
 * </ol>
 */
public final class FieldSpecComparator {
    
    /**
     * Singleton instance of the comparator
     */
    public static final Comparator<FieldSpec> INSTANCE = createComparator();

    private FieldSpecComparator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Comparator<FieldSpec> createComparator() {
        return Comparator
            .comparing((final FieldSpec f) -> !isLoggerField(f))
            // 2. Sort by visibility score
            .thenComparingInt(FieldSpecComparator::getVisibilityScore)
            // 3. Compare static
            .thenComparing(f -> !f.hasModifier(Modifier.STATIC))
            // 4. Compare final
            .thenComparing(f -> !f.hasModifier(Modifier.FINAL))
            // 5. Finally the name
            .thenComparing(m -> m.name);
    }

    private static int getVisibilityScore(final FieldSpec field) {
        if (field.hasModifier(Modifier.PUBLIC)) {
            return 0;
        } else if (field.hasModifier(Modifier.PROTECTED)) {
            return 1;
        } else if (field.hasModifier(Modifier.PRIVATE)) {
            return 3;
        } else {
            return 2; // package-private
        }
    }

    private static boolean isLoggerField(final FieldSpec field) {
        return "log".equalsIgnoreCase(field.name) 
            || "logger".equalsIgnoreCase(field.name) 
            || "__logger".equalsIgnoreCase(field.name);
    }

}

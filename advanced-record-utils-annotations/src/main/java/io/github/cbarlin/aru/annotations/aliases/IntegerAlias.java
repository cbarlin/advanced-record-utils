package io.github.cbarlin.aru.annotations.aliases;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.TypeAlias;

/**
 * An alias for {@link Integer} types, that also defers ordering to those integers.
 */
@NullMarked
public interface IntegerAlias extends TypeAlias<Integer>, Comparable<IntegerAlias> {

    @Override
    @NonNull Integer value();

    /**
     * Compares the integers that are wrapped using their ordering.
     */
    @Override
    default int compareTo(IntegerAlias o) {
        return value().compareTo(o.value());
    }

    // Implement the items in the Number abstract class
    //   ... this would be easier if Number were an interface, but alas...
    default int intValue() {
        return value().intValue();
    }

    default long longValue() {
        return value().longValue();
    }

    default float floatValue() {
        return value().floatValue();
    }

    default double doubleValue() {
        return value().doubleValue();
    }

    default byte byteValue() {
        return value().byteValue();
    }

    default short shortValue() {
        return value().shortValue();
    }
}

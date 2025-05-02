package io.github.cbarlin.aru.annotations;

import org.jspecify.annotations.NonNull;

/**
 * An alias for another kind. Useful for situations in which you wish
 *   to manage a variety of e.g. numeric IDs and use Java's type system to
 *   not get them confused.
 */
public interface TypeAlias<T> {
    /**
     * Returns the non-null value held by this alias.
     * 
     * @return The value held by the alias.
     */
    @NonNull
    T value();
}

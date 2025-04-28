package io.github.cbarlin.aru.annotations;

/**
 * An alias for another kind. Useful for situations in which you wish
 *   to manage a variety of e.g. numeric IDs and use Java's type system to
 *   not get them confused.
 */
public interface TypeAlias<T> {
    T value();
}

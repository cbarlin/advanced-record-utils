package io.github.cbarlin.aru.annotations.aliases;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.TypeAlias;

/**
 * A helper interface for making {@link TypeAlias} for {@link String}.
 * <p>
 * An example use case is when dealing with Strings representing different concepts that you might get confused
 *   (e.g. "BookName" vs "AuthorName" vs "PublisherName").
 * <p>
 * Defers operations from {@link CharSequence} and {@link Comparable} to the wrapped String.
 */
@NullMarked
public interface StringAlias extends TypeAlias<String>, CharSequence, Comparable<StringAlias> {

    /**
     * @inheritdoc
     */
    @Override
    @NonNull String value();

    /**
     * @inheritdoc
     */
    @Override
    default int length() {
        return value().length();
    }

    /**
     * @inheritdoc
     */
    @Override
    default char charAt(int index) {
        return value().charAt(index);
    }

    /**
     * @inheritdoc
     */
    @Override
    default CharSequence subSequence(int start, int end) {
        return value().subSequence(start, end);
    }

    /**
     * Compares the Strings that are wrapped using their ordering.
     */
    @Override
    default int compareTo(StringAlias o) {
        return value().compareTo(o.value());
    }
}

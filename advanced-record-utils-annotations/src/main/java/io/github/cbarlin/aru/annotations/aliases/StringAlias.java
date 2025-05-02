package io.github.cbarlin.aru.annotations.aliases;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import io.github.cbarlin.aru.annotations.TypeAlias;

/**
 * A helper interface for making {@link TypeAlias} for {@link String}.
 * <p>
 * An example use case is when dealing with Strings representing different concepts that you might get confused
 *   (e.g. "BookName" vs "AuthorName" vs "PublisherName")
 */
@NullMarked
public interface StringAlias extends TypeAlias<String>, CharSequence, Comparable<StringAlias> {

    @Override
    @NonNull String value();

    @Override
    default int length() {
        return value().length();
    }

    @Override
    default char charAt(int index) {
        return value().charAt(index);
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return value().subSequence(start, end);
    }

    @Override
    default int compareTo(StringAlias o) {
        return value().compareTo(o.value());
    }
}

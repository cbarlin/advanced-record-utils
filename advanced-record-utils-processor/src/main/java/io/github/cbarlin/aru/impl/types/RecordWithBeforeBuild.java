package io.github.cbarlin.aru.impl.types;

import javax.lang.model.element.ExecutableElement;

/**
 * Denotes that a record has a method that should be called before it is built
 *
 * @param beforeBuild The method to call before the item is built
 */
public record RecordWithBeforeBuild (
    ExecutableElement beforeBuild
) {
}

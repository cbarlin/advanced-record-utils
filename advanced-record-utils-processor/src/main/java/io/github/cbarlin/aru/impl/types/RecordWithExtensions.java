package io.github.cbarlin.aru.impl.types;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

/**
 * Denotes that a record has methods that should be added to the builder
 * <p>
 * Note that it doesn't matter the order in which the list is made as the methods
 *  are sorted before being written out.
 * @param extensionMethods The methods that should be added
 */
public record RecordWithExtensions (
    List<ExtensionMethod> extensionMethods
) {
}

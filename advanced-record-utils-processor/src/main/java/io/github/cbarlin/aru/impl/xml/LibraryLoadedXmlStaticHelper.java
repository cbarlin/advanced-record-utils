package io.github.cbarlin.aru.impl.xml;

import io.github.cbarlin.aru.core.types.LibraryLoadedTarget;
import io.micronaut.sourcegen.javapoet.ClassName;

public record LibraryLoadedXmlStaticHelper(
    ClassName helperClassName,
    LibraryLoadedTarget libraryLoadedTarget
) {
}

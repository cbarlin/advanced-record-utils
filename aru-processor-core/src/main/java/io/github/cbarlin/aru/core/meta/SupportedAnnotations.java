package io.github.cbarlin.aru.core.meta;

import java.util.HashSet;

public record SupportedAnnotations (
    HashSet<String> annotations
) {

}

package io.github.cbarlin.aru.impl.types;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class OverridingElementsFinder {
    private static final Map<ExecutableElement, List<ExecutableElement>> OVERRIDERS = new ConcurrentHashMap<>();

    private OverridingElementsFinder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static List<ExecutableElement> detectOverrides(final ExecutableElement executableElement) {
        Stream.of(executableElement)
            .map(ExecutableElement::getEnclosingElement)
            .filter(TypeElement.class::isInstance)
            .map(TypeElement.class::cast);

        return List.of();
    }

    public static List<ExecutableElement> findOverrides(final ExecutableElement executableElement) {
        return OVERRIDERS.computeIfAbsent(executableElement, OverridingElementsFinder::detectOverrides);
    }
}

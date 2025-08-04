package io.github.cbarlin.aru.core.factories;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtilsFull;
import io.github.cbarlin.aru.annotations.TypeConverter;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Factory
@CoreGlobalScope
public final class SupportedAnnotationsFactory {

    @Bean
    @CoreGlobalScope
    SupportedAnnotations initialAnnotations() throws IOException {
        final HashSet<String> supportedAnnotations = new HashSet<>();
        supportedAnnotations.add(AdvancedRecordUtils.class.getCanonicalName());
        supportedAnnotations.add(AdvancedRecordUtilsFull.class.getCanonicalName());
        supportedAnnotations.add(TypeConverter.class.getCanonicalName());
        return new SupportedAnnotations(loadAnnotations(supportedAnnotations));
    }

    private static HashSet<TypeElement> loadAnnotations(final Set<String> names) {
        final var res = names.stream()
                .map(APContext.elements()::getTypeElement)
                .filter(Objects::nonNull)
                .map(ClassName::get)
                .map(OptionalClassDetector::loadAnnotation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableSet());
        return new HashSet<>(res);
    }
}

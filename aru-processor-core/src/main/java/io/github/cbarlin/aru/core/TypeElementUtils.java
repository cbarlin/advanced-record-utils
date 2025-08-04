package io.github.cbarlin.aru.core;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micronaut.sourcegen.javapoet.ClassName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class TypeElementUtils {
    private static final AsyncCache<TypeElement, Set<TypeElement>> INHERITANCE_TREE = Caffeine.newBuilder()
        .initialCapacity(100)
        .buildAsync();

    static {
        // Because we know the answer to these
        OptionalClassDetector.optionalDependencyTypeElement(ClassName.get(Object.class))
            .ifPresent(te -> INHERITANCE_TREE.put(te, CompletableFuture.completedFuture(Set.of())));
        OptionalClassDetector.optionalDependencyTypeElement(ClassName.get(Record.class))
            .ifPresent(te -> INHERITANCE_TREE.put(te, CompletableFuture.completedFuture(Set.of())));
        // Skip these because they contain nothing useful
        OptionalClassDetector.optionalDependencyTypeElement(CommonsConstants.Names.GENERATED_UTIL)
            .ifPresent(te -> INHERITANCE_TREE.put(te, CompletableFuture.completedFuture(Set.of())));
        OptionalClassDetector.optionalDependencyTypeElement(CommonsConstants.Names.TYPE_ALIAS)
            .ifPresent(te -> INHERITANCE_TREE.put(te, CompletableFuture.completedFuture(Set.of())));
    }

    public static Set<TypeElement> obtainInheritedItems (final @Nullable TypeElement typeElement) {
        if (Objects.isNull(typeElement)) {
            return Set.of();
        }
        final CompletableFuture<Set<TypeElement>> future = new CompletableFuture<>();
        final var prior = INHERITANCE_TREE.asMap().putIfAbsent(typeElement, future);
        if (Objects.nonNull(prior)) {
            return prior.join();
        }
        final Set<TypeElement> result = obtainInheritingElements(typeElement);
        future.complete(result);
        return result;
    }

    private TypeElementUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Set<TypeElement> obtainInheritingElements(final TypeElement typeElement) {
        final Set<TypeElement> found = HashSet.newHashSet(5);
        OptionalClassDetector.optionalDependencyTypeElement(typeElement.getSuperclass())
            .ifPresent(par -> {
                found.add(par);
                found.addAll(obtainInheritedItems(par));
            });
        Objects.requireNonNullElse(typeElement.getInterfaces(), List.<TypeMirror>of()).stream()
            .map(OptionalClassDetector::optionalDependencyTypeElement)
            .filter(Optional::isPresent)
            .map(Optional::get)
            // Don't re-add things we already found
            .filter(found::add)
            .flatMap(par -> obtainInheritedItems(par).stream())
            .forEach(found::add);
        return Set.copyOf(found);
    }
}

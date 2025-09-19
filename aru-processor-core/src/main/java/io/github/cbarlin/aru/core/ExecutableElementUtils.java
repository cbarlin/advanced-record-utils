package io.github.cbarlin.aru.core;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.cbarlin.aru.annotations.Generated;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ExecutableElementUtils {
    private static final AsyncCache<ExecutableElement, Set<ExecutableElement>> OVERRIDE_TREE = Caffeine.newBuilder()
        .initialCapacity(100)
        .buildAsync();

    @Generated("CONSTRUCTOR_STATIC_CLASS")
    private ExecutableElementUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Set<ExecutableElement> obtainOverrideTree (final @Nullable ExecutableElement executableElement) {
        if (Objects.isNull(executableElement)) {
            return Set.of();
        }
        final CompletableFuture<Set<ExecutableElement>> future = new CompletableFuture<>();
        final var prior = OVERRIDE_TREE.asMap().putIfAbsent(executableElement, future);
        if (Objects.nonNull(prior)) {
            return prior.join();
        }
        final Set<ExecutableElement> result = findOverridesImpl(executableElement);
        future.complete(result);
        return result;
    }

    private static final Set<ExecutableElement> findOverridesImpl(final ExecutableElement executableElement) {
        final Set<ExecutableElement> found = HashSet.newHashSet(2);
        final var optT = Optional.ofNullable(executableElement.getEnclosingElement())
            .filter(TypeElement.class::isInstance)
            .map(TypeElement.class::cast);
        if (optT.isEmpty()) {
            return Set.of();
        }
        final TypeElement typeElement = optT.get();
        findOverrideOn(typeElement.asType(), executableElement, typeElement)
            .ifPresent(overrider -> {
                found.add(overrider);
                found.addAll(obtainOverrideTree(overrider));
            });

        return Set.copyOf(found);
    }

    private static final Optional<ExecutableElement> findOverrideOn(final @Nullable TypeMirror mirror, final ExecutableElement executableElement, final TypeElement originalTypeElement) {
        final Optional<TypeElement> optionalTypeElement = OptionalClassDetector.optionalDependencyTypeElement(mirror);
        if (optionalTypeElement.isEmpty()) {
            return Optional.empty();
        }
        final TypeElement typeElement = optionalTypeElement.get();
        for (final Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement instanceof final ExecutableElement otherExec && APContext.elements().overrides(executableElement, otherExec, originalTypeElement)) {
                return Optional.of(otherExec);
            }
        }
        // Check interfaces
        for (final TypeMirror iface : Objects.requireNonNullElse(typeElement.getInterfaces(), List.<TypeMirror>of())) {
            final Optional<TypeElement> ifTe = OptionalClassDetector.optionalDependencyTypeElement(iface);
            if (ifTe.isEmpty()) {
                continue;
            }
            final TypeElement implIface = ifTe.get();
            for (final Element enclosedElement : implIface.getEnclosedElements()) {
                if (enclosedElement instanceof final ExecutableElement otherExec && APContext.elements().overrides(executableElement, otherExec, originalTypeElement)) {
                    return Optional.of(otherExec);
                }
            }
            final var parentIface = findOverrideOn(iface, executableElement, originalTypeElement);
            if (parentIface.isPresent()) {
                return parentIface;
            }
        }
        // Recurse to parent type
        return findOverrideOn(typeElement.getSuperclass(), executableElement, originalTypeElement);
    }
}

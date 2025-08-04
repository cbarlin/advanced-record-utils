package io.github.cbarlin.aru.core;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import java.util.Objects;
import java.util.Optional;

public final class PackageUtils {

    private PackageUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final LoadingCache<PackageElement, Optional<PackageElement>> PARENT_PACKAGE = Caffeine.newBuilder()
        .initialCapacity(20)
        .build(PackageUtils::deriveParent);

    public static Optional<PackageElement> getParentPackage(final Element element) {
        if (element instanceof final PackageElement packageElement) {
            return PARENT_PACKAGE.get(packageElement);
        } else {
            return Optional.ofNullable(APContext.elements().getPackageOf(element));
        }
    }

    private static Optional<PackageElement> deriveParent(final PackageElement packageElement) {
        final String fqn = packageElement.getQualifiedName().toString();
        if (Objects.nonNull(fqn)) {
            final int dotPos = fqn.lastIndexOf('.');
            if (dotPos != -1) {
                return Optional.ofNullable(APContext.elements().getPackageElement(fqn.substring(0, dotPos)));
            }
        }
        return Optional.empty();
    }
}

package io.github.cbarlin.aru.core.artifacts;
import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.ClaimableOperation;

import io.micronaut.sourcegen.javapoet.ClassName;

public interface GenerationArtifact<T extends GenerationArtifact<T>> {

    public ClassName className();

    @Nullable
    public T childArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation);

    default void cleanup() {
        // No-op
    }
}

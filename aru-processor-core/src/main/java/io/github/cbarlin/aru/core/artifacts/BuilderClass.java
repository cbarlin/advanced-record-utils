package io.github.cbarlin.aru.core.artifacts;

public record BuilderClass(
    ToBeBuilt delegate
) implements IToBeBuilt<ToBeBuilt> {}

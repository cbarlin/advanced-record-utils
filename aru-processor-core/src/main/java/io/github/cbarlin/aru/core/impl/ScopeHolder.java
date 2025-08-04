package io.github.cbarlin.aru.core.impl;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.BeanScope;

public final class ScopeHolder implements AutoCloseable {

    private final BeanScope fullScope;
    private final @Nullable BeanScope parentScope;

    public ScopeHolder(final BeanScope fullScope, final @Nullable BeanScope parentScope) {
        this.fullScope = fullScope;
        this.parentScope = parentScope;
    }

    public BeanScope scope() {
        return fullScope;
    }

    @Override
    public void close() {
        fullScope.close();
        if (Objects.nonNull(parentScope)) {
            parentScope.close();
        }
    }
    
}

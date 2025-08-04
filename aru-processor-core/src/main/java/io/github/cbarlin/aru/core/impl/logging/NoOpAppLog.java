package io.github.cbarlin.aru.core.impl.logging;

import io.avaje.applog.AppLog;
import io.avaje.spi.ServiceProvider;

import java.util.ResourceBundle;

@ServiceProvider
public final class NoOpAppLog implements AppLog.Provider {
    @Override
    public System.Logger getLogger(String name) {
        return new NoOpLogger(name);
    }

    @Override
    public System.Logger getLogger(String name, ResourceBundle bundle) {
        return new NoOpLogger(name);
    }
}

package io.github.cbarlin.aru.core.impl.logging;

import java.util.ResourceBundle;

public record NoOpLogger(String name) implements System.Logger {
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLoggable(Level level) {
        return true;
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {

    }

    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params) {

    }
}

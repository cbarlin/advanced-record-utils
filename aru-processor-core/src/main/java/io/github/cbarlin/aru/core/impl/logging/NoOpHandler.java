package io.github.cbarlin.aru.core.impl.logging;

import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class NoOpHandler extends Handler {

    public static void install() {
        final Logger rootLogger  = LogManager.getLogManager().getLogger("");
        final Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        final NoOpHandler handler = new NoOpHandler();
        LogManager.getLogManager().getLogger("").addHandler(handler);
    }

    @Override
    public void publish(LogRecord record) {
        // No-Op
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }
}

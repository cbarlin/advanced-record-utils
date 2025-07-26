package io.github.cbarlin.aru.core.wiring;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Factory
@CoreGlobalScope
public final class ExecutorFactory {

    private static final int THREAD_COUNT = 4;

    @Bean
    ExecutorService executorService () {
        return Executors.newWorkStealingPool(THREAD_COUNT);
    }
}

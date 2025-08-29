/**
 * The package that provides implementations of all the non-core work the
 * processor does. If
 * you are looking for details about the processor, and it's usage, please see
 * <a href="https://github.com/cbarlin/advanced-record-utils">the GitHub page</a>
 * <p>
 * While the size of this package might seem quite daunting, each individual
 * file should be:
 * <ul>
 * <li>Self-contained; and</li>
 * <li>Quite small</li>
 * </ul>
 * <p>
 * This package is sub-divided into the different things that the processor can
 * do, and a "Constants" file.
 */

@org.jspecify.annotations.NullMarked
module io.github.cbarlin.aru.worker {
    requires io.github.cbarlin.aru.core;
    
    requires transitive io.github.cbarlin.aru.prism.prison;
    requires transitive java.compiler;

    requires static io.avaje.spi;
    requires static org.jspecify;
    requires static org.mapstruct.processor;

    provides io.github.cbarlin.aru.core.wiring.InjectModuleFinder with
        io.github.cbarlin.aru.impl.wiring.InjectModuleProvider;

    provides org.mapstruct.ap.spi.BuilderProvider with
        io.github.cbarlin.aru.impl.AruMapStructBuilderProvider;
}

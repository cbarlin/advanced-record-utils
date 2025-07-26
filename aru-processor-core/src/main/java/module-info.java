/**
 * The core of the processor logic. If you are looking for information about the annotations or usage,
 *   you should check out <a href="https://github.com/cbarlin/advanced-record-utils">the GitHub Repo</a>
 * <p>
 * As this is the core of the processor, it's quite dense but doesn't do a lot of
 *   user-visible work (just a very basic builder!) 
 */

@org.jspecify.annotations.NullMarked
module io.github.cbarlin.aru.core {
    exports io.github.cbarlin.aru.core;
    exports io.github.cbarlin.aru.core.artifacts;
    exports io.github.cbarlin.aru.core.mirrorhandlers;
    exports io.github.cbarlin.aru.core.types;
    exports io.github.cbarlin.aru.core.types.components;
    exports io.github.cbarlin.aru.core.visitors;
    exports io.github.cbarlin.aru.core.wiring;

    requires transitive io.avaje.inject;
    requires transitive io.github.cbarlin.aru.annotations;
    requires transitive io.github.cbarlin.aru.prism.prison;
    requires transitive io.micronaut.sourcegen.sourcegen_generator_java;
    requires transitive java.compiler;
    requires transitive org.apache.commons.lang3;
    requires static io.avaje.prism;
    requires static io.avaje.spi;
    requires static org.jspecify;

    uses io.github.cbarlin.aru.core.wiring.InjectModuleFinder;

    provides javax.annotation.processing.Processor with
        io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
        
    provides javax.annotation.processing.AbstractProcessor with
        io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
}

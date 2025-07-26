/**
 * The core of the processor logic. If you are looking for information about the annotations or usage,
 *   you should check out https://github.com/cbarlin/advanced-record-utils
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
    exports io.github.cbarlin.aru.core.visitors;
    exports io.github.cbarlin.aru.core.impl.types;
    exports io.github.cbarlin.aru.core.wiring;

    requires transitive io.github.cbarlin.aru.annotations;
    requires transitive io.github.cbarlin.aru.prism.prison;
    requires transitive java.compiler;
    requires transitive io.micronaut.sourcegen.sourcegen_generator_java;
    requires transitive org.apache.commons.lang3;
    requires transitive io.avaje.inject;
    requires static io.avaje.prism;
    requires static io.avaje.spi;
    requires static org.jspecify;

    uses io.github.cbarlin.aru.core.inference.AnnotationInferencer;
    uses io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;

    provides io.avaje.inject.spi.InjectExtension with
        io.github.cbarlin.aru.core.AruCoreModule;

    provides javax.annotation.processing.Processor with
        io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
        
    provides javax.annotation.processing.AbstractProcessor with
        io.github.cbarlin.aru.core.AdvRecUtilsProcessor;
}

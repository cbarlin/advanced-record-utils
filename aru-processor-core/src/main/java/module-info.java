module io.github.cbarlin.aru.core {
    exports io.github.cbarlin.aru.core;
    exports io.github.cbarlin.aru.core.artifacts;
    exports io.github.cbarlin.aru.core.mirrorhandlers;
    exports io.github.cbarlin.aru.core.types;
    exports io.github.cbarlin.aru.core.visitors;
    exports io.github.cbarlin.aru.core.visitors.collection;
    exports io.github.cbarlin.aru.core.impl.types;

    requires transitive io.github.cbarlin.aru.annotations;
    requires transitive io.github.cbarlin.aru.prism.prison;
    requires transitive java.compiler;
    requires transitive io.micronaut.sourcegen.sourcegen_generator_java;
    requires transitive org.apache.commons.lang3;
    requires static io.avaje.prism;
    requires static io.avaje.spi;
    requires static org.jspecify;

    provides javax.annotation.processing.Processor with io.github.cbarlin.aru.core.AdvRecUtilsProcessor;

    uses io.github.cbarlin.aru.core.visitors.RecordVisitor;
    uses io.github.cbarlin.aru.core.types.ComponentAnalyser;
    uses io.github.cbarlin.aru.core.inference.AnnotationInferencer;
    uses io.github.cbarlin.aru.core.inference.ClassNameToPrismAdaptor;

    provides io.github.cbarlin.aru.core.visitors.RecordVisitor
            with io.github.cbarlin.aru.core.impl.visitors.BuilderClassCreatorVisitor,
            io.github.cbarlin.aru.core.impl.visitors.builder.AddCopyConstruction,
            io.github.cbarlin.aru.core.impl.visitors.builder.AddEmptyConstruction,
            io.github.cbarlin.aru.core.impl.visitors.builder.AddField,
            io.github.cbarlin.aru.core.impl.visitors.builder.AddGetter,
            io.github.cbarlin.aru.core.impl.visitors.builder.AddPlainBuild,
            io.github.cbarlin.aru.core.impl.visitors.builder.AddSetter,
            io.github.cbarlin.aru.core.impl.visitors.builder.collection.AddListFieldNeverNull,
            io.github.cbarlin.aru.core.impl.visitors.builder.collection.AddListNeverNullGetter,
            io.github.cbarlin.aru.core.impl.visitors.builder.collection.AddListSingleAdder,
            io.github.cbarlin.aru.core.impl.visitors.builder.collection.AddSetFieldNeverNull,
            io.github.cbarlin.aru.core.impl.visitors.builder.collection.AddSetNeverNullGetter,
            io.github.cbarlin.aru.core.impl.visitors.builder.collection.AddSetSingleAdder;

    provides io.github.cbarlin.aru.core.types.ComponentAnalyser 
            with io.github.cbarlin.aru.core.impl.types.analyser.BasicAnalyser,
            io.github.cbarlin.aru.core.impl.types.analyser.JavaNonMapCollectionAnalyser,
            io.github.cbarlin.aru.core.impl.types.analyser.OptionalAnalyser;
}

/**
 * Core structure and processing operations for Advanced Record Utils
 */
@org.jspecify.annotations.NullMarked
@io.avaje.inject.InjectModule(
    name = "AruCore",
    requires = {
        javax.annotation.processing.ProcessingEnvironment.class
    },
    provides = {
        io.github.cbarlin.aru.core.types.AnalysedType.class,
        io.github.cbarlin.aru.core.types.AnalysedRecord.class,
        io.github.cbarlin.aru.core.types.AnalysedInterface.class,
        io.github.cbarlin.aru.core.AdvRecUtilsSettings.class,
        io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism.class,
        javax.lang.model.element.RecordComponentElement.class
    },
    strictWiring = false
)
package io.github.cbarlin.aru.core;

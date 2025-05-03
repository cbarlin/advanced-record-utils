/**
 * This module only contains prisms - it likely isn't what you are looking for.
 * <p>
 * For information on the processor and usage, please see https://github.com/cbarlin/advanced-record-utils
 * <p>
 * For information on the prisms and what they look like etc, please see https://avaje.io/prisms/
 */
module io.github.cbarlin.aru.prism.prison {
    exports io.github.cbarlin.aru.prism.prison;
    requires static io.avaje.prism;
    requires static io.avaje.jsonb;
    requires static com.fasterxml.jackson.annotation;
    requires static jakarta.xml.bind;
    requires java.compiler;
}

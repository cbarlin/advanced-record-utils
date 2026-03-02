package io.github.cbarlin.aru.tests.a_core_dependency.hidden;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;

@AdvancedRecordUtils(logGeneration = LoggingGeneration.SLF4J_GENERATED_UTIL_INTERFACE)
public @interface AnotherMetaAnnotation {

}

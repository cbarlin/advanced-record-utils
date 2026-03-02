package io.github.cbarlin.aru.tests.a_core_dependency.hidden;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@AdvancedRecordUtils(logGeneration = LoggingGeneration.SLF4J_GENERATED_UTIL_INTERFACE)
@Target({TYPE})
@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface AnotherMetaAnnotation {

}

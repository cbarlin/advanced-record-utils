package io.github.cbarlin.aru.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuiltCollectionType;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;

/**
 * A version of the utils generation that enables most options
 */
@Target({PACKAGE, TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.CLASS)
@Inherited
@AdvancedRecordUtils(
    // Only applies if this is applied to a package
    applyToAllInPackage = true,
    merger = true,
    xmlable = true,
    logGeneration = LoggingGeneration.SLF4J_UTILS_CLASS,
    builderOptions = @BuilderOptions(
        builtCollectionType = BuiltCollectionType.JAVA_IMMUTABLE
    )
)
public @interface AdvancedRecordUtilsFull {

}

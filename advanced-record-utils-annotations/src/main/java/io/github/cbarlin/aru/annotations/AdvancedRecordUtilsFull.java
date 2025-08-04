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
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DiffOptions;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.MergerOptions;

/**
 * A version of the utils generation that enables most options
 */
@Target({PACKAGE, TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.CLASS)
@Inherited
@AdvancedRecordUtils(
    // Only applies if this is applied to a package
    applyToAllInPackage = true,
    createAllInterface = true,
    diffable = true,
    merger = true,
    wither = true,
    xmlable = true,
    logGeneration = LoggingGeneration.SLF4J_UTILS_CLASS,
    builderOptions = @BuilderOptions(
        builtCollectionType = BuiltCollectionType.JAVA_IMMUTABLE
    ),
    diffOptions = @DiffOptions(
        staticMethodsAddedToUtils = true
    ),
    mergerOptions = @MergerOptions(
        staticMethodsAddedToUtils = true
    )
)
public @interface AdvancedRecordUtilsFull {

}

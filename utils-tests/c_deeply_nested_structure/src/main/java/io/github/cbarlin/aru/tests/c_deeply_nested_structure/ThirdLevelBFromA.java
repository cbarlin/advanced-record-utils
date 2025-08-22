package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import jakarta.xml.bind.annotation.XmlAttribute;

@AdvancedRecordUtils(
    diffable = true,
    addJsonbImportAnnotation = true,
    logGeneration = AdvancedRecordUtils.LoggingGeneration.SLF4J_GENERATED_UTIL_INTERFACE,
    diffOptions = @AdvancedRecordUtils.DiffOptions(staticMethodsAddedToUtils = true),
    builderOptions = @AdvancedRecordUtils.BuilderOptions(
        builtCollectionType = AdvancedRecordUtils.BuiltCollectionType.JAVA_IMMUTABLE,
        setTimeNowMethodSuffix = "ToCurrent",
        buildMethodName = "make",
        setTimeNowMethodPrefix = "update",
        setToNullMethods = true
    ),
    xmlable = true,
    witherOptions = @AdvancedRecordUtils.WitherOptions(convertToBuilder = "toBuilder"),
    createAllInterface = true,
    wither = true,
    merger = true
)
public record ThirdLevelBFromA(
    @XmlAttribute
    int someValue
) implements ThirdLevelInterface {

}

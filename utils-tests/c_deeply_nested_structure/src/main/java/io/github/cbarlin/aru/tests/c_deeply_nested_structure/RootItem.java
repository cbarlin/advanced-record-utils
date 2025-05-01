package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import java.time.OffsetDateTime;
import java.util.List;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuiltCollectionType;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.WitherOptions;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@AdvancedRecordUtils(
    wither = true,
    merger = true,
    xmlable = true,
    createAllInterface = true,
    logGeneration = LoggingGeneration.SLF4J_GENERATED_UTIL_INTERFACE,
    witherOptions = @WitherOptions(convertToBuilder = "toBuilder"),
    builderOptions = @BuilderOptions(builtCollectionType = BuiltCollectionType.JAVA_IMMUTABLE),
    addJsonbImportAnnotation = true
)
@XmlRootElement(name = "RootItem", namespace = "ns://nxA")
@XmlType(name = "", propOrder = {"yetAnotherField"})
public record RootItem(
    @XmlElement(name = "FirstLevels")
    List<FirstLevel> firstLevels,
    @XmlAttribute
    int anotherField,
    @XmlAttribute(name = "butIHaveAnotherName", required = true)
    String yetAnotherField,
    @XmlAttribute
    OffsetDateTime testOdtAttr,
    @XmlElement
    OffsetDateTime testOdtEl
) implements RootItemUtils.All {}

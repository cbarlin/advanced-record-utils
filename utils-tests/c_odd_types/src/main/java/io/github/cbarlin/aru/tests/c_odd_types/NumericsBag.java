package io.github.cbarlin.aru.tests.c_odd_types;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@AdvancedRecordUtils(
    xmlable = true,
    wither = true,
    merger = true,
    logGeneration = LoggingGeneration.SLF4J_GENERATED_UTIL_INTERFACE,
    createAllInterface = true
)
@XmlRootElement(
    name = "NumericsTest",
    namespace = "nx://Numerics"
)
// Reminder: Default values are written literally - they are not converted at all
public record NumericsBag(
    // Bytes
    @XmlAttribute(name = "primitiveByteAttribute")
    byte primitiveByteAttribute,
    @XmlAttribute(name = "boxedByteAttribute")
    Byte boxedByteAttribute,
    @XmlAttribute(name = "boxedByteAttributeRequired", required = true)
    Byte boxedByteAttributeRequired,
    @XmlElement(name = "primitiveByteElement", required = true)
    byte primitiveByteElement,
    @XmlElement(name = "boxedByteElement", required = true)
    Byte boxedByteElement,
    @XmlElement(name = "boxedByteElementRequired", required = true)
    Byte boxedByteElementRequired,
    @XmlElement(name = "boxedByteElementDefault", defaultValue = "1")
    Byte boxedByteElementDefault,
    @XmlElement(name = "boxedByteElementRequiredDefault", required = true, defaultValue = "1")
    Byte boxedByteElementRequiredDefault,
    // Char
    @XmlAttribute(name = "primitiveCharAttribute")
    char primitiveCharAttribute,
    @XmlAttribute(name = "boxedCharAttribute")
    Character boxedCharAttribute,
    @XmlAttribute(name = "boxedCharAttributeRequired", required = true)
    Character boxedCharAttributeRequired,
    @XmlElement(name = "primitiveCharElement", required = true)
    char primitiveCharElement,
    @XmlElement(name = "boxedCharElement", required = true)
    Character boxedCharElement,
    @XmlElement(name = "boxedCharElementRequired", required = true)
    Character boxedCharElementRequired,
    @XmlElement(name = "boxedCharElementDefault", defaultValue = "1")
    Character boxedCharElementDefault,
    @XmlElement(name = "boxedCharElementRequiredDefault", required = true, defaultValue = "1")
    Character boxedCharElementRequiredDefault,
    // Doubles
    @XmlAttribute(name = "primitiveDoubleAttribute")
    double primitiveDoubleAttribute,
    @XmlAttribute(name = "boxedDoubleAttribute")
    Double boxedDoubleAttribute,
    @XmlAttribute(name = "boxedDoubleAttributeRequired", required = true)
    Double boxedDoubleAttributeRequired,
    @XmlElement(name = "primitiveDoubleElement", required = true)
    double primitiveDoubleElement,
    @XmlElement(name = "boxedDoubleElement", required = true)
    Double boxedDoubleElement,
    @XmlElement(name = "boxedDoubleElementRequired", required = true)
    Double boxedDoubleElementRequired,
    @XmlElement(name = "boxedDoubleElementDefault", defaultValue = "1")
    Double boxedDoubleElementDefault,
    @XmlElement(name = "boxedDoubleElementRequiredDefault", required = true, defaultValue = "1")
    Double boxedDoubleElementRequiredDefault,
    // Ints
    @XmlAttribute(name = "primitiveIntAttribute")
    int primitiveIntAttribute,
    @XmlAttribute(name = "boxedIntAttribute")
    Integer boxedIntAttribute,
    @XmlAttribute(name = "boxedIntAttributeRequired", required = true)
    Integer boxedIntAttributeRequired,
    @XmlElement(name = "primitiveIntElement", required = true)
    int primitiveIntElement,
    @XmlElement(name = "boxedIntElement", required = true)
    Integer boxedIntElement,
    @XmlElement(name = "boxedIntElementRequired", required = true)
    Integer boxedIntElementRequired,
    @XmlElement(name = "boxedIntElementDefault", defaultValue = "1")
    Integer boxedIntElementDefault,
    @XmlElement(name = "boxedIntElementRequiredDefault", required = true, defaultValue = "1")
    Integer boxedIntElementRequiredDefault,
    // Longs
    @XmlAttribute(name = "primitiveLongAttribute")
    long primitiveLongAttribute,
    @XmlAttribute(name = "boxedLongAttribute")
    Long boxedLongAttribute,
    @XmlAttribute(name = "boxedLongAttributeRequired", required = true)
    Long boxedLongAttributeRequired,
    @XmlElement(name = "primitiveLongElement", required = true)
    long primitiveLongElement,
    @XmlElement(name = "boxedLongElement", required = true)
    Long boxedLongElement,
    @XmlElement(name = "boxedLongElementRequired", required = true)
    Long boxedLongElementRequired,
    @XmlElement(name = "boxedLongElementDefault", defaultValue = "1")
    Long boxedLongElementDefault,
    @XmlElement(name = "boxedLongElementRequiredDefault", required = true, defaultValue = "1")
    Long boxedLongElementRequiredDefault,
    // Floats
    @XmlAttribute(name = "primitiveFloatAttribute")
    float primitiveFloatAttribute,
    @XmlAttribute(name = "boxedFloatAttribute")
    Float boxedFloatAttribute,
    @XmlAttribute(name = "boxedFloatAttributeRequired", required = true)
    Float boxedFloatAttributeRequired,
    @XmlElement(name = "primitiveFloatElement", required = true)
    float primitiveFloatElement,
    @XmlElement(name = "boxedFloatElement", required = true)
    Float boxedFloatElement,
    @XmlElement(name = "boxedFloatElementRequired", required = true)
    Float boxedFloatElementRequired,
    @XmlElement(name = "boxedFloatElementDefault", defaultValue = "1")
    Float boxedFloatElementDefault,
    @XmlElement(name = "boxedFloatElementRequiredDefault", required = true, defaultValue = "1")
    Float boxedFloatElementRequiredDefault,
    // Shorts
    @XmlAttribute(name = "primitiveShortAttribute")
    short primitiveShortAttribute,
    @XmlAttribute(name = "boxedShortAttribute")
    Short boxedShortAttribute,
    @XmlAttribute(name = "boxedShortAttributeRequired", required = true)
    Short boxedShortAttributeRequired,
    @XmlElement(name = "primitiveShortElement", required = true)
    short primitiveShortElement,
    @XmlElement(name = "boxedShortElement", required = true)
    Short boxedShortElement,
    @XmlElement(name = "boxedShortElementRequired", required = true)
    Short boxedShortElementRequired,
    @XmlElement(name = "boxedShortElementDefault", defaultValue = "1")
    Short boxedShortElementDefault,
    @XmlElement(name = "boxedShortElementRequiredDefault", required = true, defaultValue = "1")
    Short boxedShortElementRequiredDefault,
    // BigInteger
    @XmlAttribute(name = "bigIntegerAttribute")
    BigInteger bigIntegerAttribute,
    @XmlAttribute(name = "bigIntegerAttributeRequired", required = true)
    BigInteger bigIntegerAttributeRequired,
    @XmlElement(name = "bigIntegerElement", required = true)
    BigInteger bigIntegerElement,
    @XmlElement(name = "bigIntegerElementRequired", required = true)
    BigInteger bigIntegerElementRequired,
    @XmlElement(name = "bigIntegerElementDefault", defaultValue = "1")
    BigInteger bigIntegerElementDefault,
    @XmlElement(name = "bigIntegerElementRequiredDefault", required = true, defaultValue = "1")
    BigInteger bigIntegerElementRequiredDefault,
    // BigDecimal
    @XmlAttribute(name = "bigDecimalAttribute")
    BigDecimal bigDecimalAttribute,
    @XmlAttribute(name = "bigDecimalAttributeRequired", required = true)
    BigDecimal bigDecimalAttributeRequired,
    @XmlElement(name = "bigDecimalElement", required = true)
    BigDecimal bigDecimalElement,
    @XmlElement(name = "bigDecimalElementRequired", required = true)
    BigDecimal bigDecimalElementRequired,
    @XmlElement(name = "bigDecimalElementDefault", defaultValue = "1")
    BigDecimal bigDecimalElementDefault,
    @XmlElement(name = "bigDecimalElementRequiredDefault", required = true, defaultValue = "1")
    BigDecimal bigDecimalElementRequiredDefault
) implements NumericsBagUtils.All {}

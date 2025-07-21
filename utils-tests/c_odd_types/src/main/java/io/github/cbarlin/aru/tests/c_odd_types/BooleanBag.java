package io.github.cbarlin.aru.tests.c_odd_types;

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
    name = "BooleanTest",
    namespace = "nx://Booleans"
)
public record BooleanBag (
    // Booleans
    @XmlAttribute(name = "primitiveBooleanAttribute")
    boolean primitiveBooleanAttribute,
    @XmlAttribute(name = "boxedBooleanAttribute")
    Boolean boxedBooleanAttribute,
    @XmlAttribute(name = "boxedBooleanAttributeRequired", required = true)
    Boolean boxedBooleanAttributeRequired,
    @XmlElement(name = "primitiveBooleanElement", required = true)
    boolean primitiveBooleanElement,
    @XmlElement(name = "boxedBooleanElement")
    Boolean boxedBooleanElement,
    @XmlElement(name = "boxedBooleanElementRequired", required = true)
    Boolean boxedBooleanElementRequired,
    @XmlElement(name = "boxedBooleanElementDefault", defaultValue = "false")
    Boolean boxedBooleanElementDefault,
    @XmlElement(name = "boxedBooleanElementRequiredDefault", required = true, defaultValue = "false")
    Boolean boxedBooleanElementRequiredDefault
) implements BooleanBagUtils.All {

}
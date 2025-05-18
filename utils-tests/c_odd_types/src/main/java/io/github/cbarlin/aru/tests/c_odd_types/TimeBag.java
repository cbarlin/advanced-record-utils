package io.github.cbarlin.aru.tests.c_odd_types;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

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
    name = "TimeBag",
    namespace = "nx://TimeTests"
)
public record TimeBag(
    @XmlAttribute(name = "offsetDateTimeAttribute")
    OffsetDateTime offsetDateTimeAttribute,
    @XmlAttribute(name = "offsetDateTimeAttributeRequired", required = true)
    OffsetDateTime offsetDateTimeAttributeRequired,
    @XmlElement(name = "offsetDateTimeElement")
    OffsetDateTime offsetDateTimeElement,
    @XmlElement(name = "offsetDateTimeElementDefault", defaultValue = "1999-01-02T03:04:05.0001")
    OffsetDateTime offsetDateTimeElementDefault,
    @XmlElement(name = "offsetDateTimeElementRequired", required = true)
    OffsetDateTime offsetDateTimeElementRequired,
    @XmlElement(name = "offsetDateTimeElementRequiredDefault", required = true, defaultValue = "1999-01-02T03:04:55.0001")
    OffsetDateTime offsetDateTimeElementRequiredDefault,

    @XmlAttribute(name = "zonedDateTimeAttribute")
    ZonedDateTime zonedDateTimeAttribute,
    @XmlAttribute(name = "zonedDateTimeAttributeRequired", required = true)
    ZonedDateTime zonedDateTimeAttributeRequired,
    @XmlElement(name = "zonedDateTimeElement")
    ZonedDateTime zonedDateTimeElement,
    @XmlElement(name = "zonedDateTimeElementDefault", defaultValue = "1999-01-02T03:04:05.0001")
    ZonedDateTime zonedDateTimeElementDefault,
    @XmlElement(name = "zonedDateTimeElementRequired", required = true)
    ZonedDateTime zonedDateTimeElementRequired,
    @XmlElement(name = "zonedDateTimeElementRequiredDefault", required = true, defaultValue = "1999-01-02T03:04:55.0001")
    ZonedDateTime zonedDateTimeElementRequiredDefault,

    @XmlAttribute(name = "localDateTimeAttribute")
    LocalDateTime localDateTimeAttribute,
    @XmlAttribute(name = "localDateTimeAttributeRequired", required = true)
    LocalDateTime localDateTimeAttributeRequired,
    @XmlElement(name = "localDateTimeElement")
    LocalDateTime localDateTimeElement,
    @XmlElement(name = "localDateTimeElementDefault", defaultValue = "1999-01-02T03:04:05.0001")
    LocalDateTime localDateTimeElementDefault,
    @XmlElement(name = "localDateTimeElementRequired", required = true)
    LocalDateTime localDateTimeElementRequired,
    @XmlElement(name = "localDateTimeElementRequiredDefault", required = true, defaultValue = "1999-01-02T03:04:55.0001")
    LocalDateTime localDateTimeElementRequiredDefault
) implements TimeBagUtils.All {

}

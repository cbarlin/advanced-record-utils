package io.github.cbarlin.aru.tests.c_odd_types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

class TimeTest {

    private static final ZonedDateTime TEST_TIME = ZonedDateTime.of(2025, 05, 3, 20, 27, 18, 44, ZoneId.of("Australia/Sydney"));

    TimeBag buildNoDefaults() {
        return TimeBagUtils.builder()
            .offsetDateTimeAttribute(TEST_TIME.toOffsetDateTime())
            .offsetDateTimeAttributeRequired(TEST_TIME.toOffsetDateTime())
            .offsetDateTimeElement(TEST_TIME.toOffsetDateTime())
            .offsetDateTimeElementRequired(TEST_TIME.toOffsetDateTime())
            .zonedDateTimeAttribute(TEST_TIME)
            .zonedDateTimeAttributeRequired(TEST_TIME)
            .zonedDateTimeElement(TEST_TIME)
            .zonedDateTimeElementRequired(TEST_TIME)
            .localDateTimeAttribute(TEST_TIME.toLocalDateTime())
            .localDateTimeAttributeRequired(TEST_TIME.toLocalDateTime())
            .localDateTimeElement(TEST_TIME.toLocalDateTime())
            .localDateTimeElementRequired(TEST_TIME.toLocalDateTime())
            .build();
    }

    TimeBag buildWithDefaults() {
        return buildNoDefaults().with()
            .offsetDateTimeElementDefault(TEST_TIME.toOffsetDateTime())
            .offsetDateTimeElementRequiredDefault(TEST_TIME.toOffsetDateTime())
            .zonedDateTimeElementDefault(TEST_TIME)
            .zonedDateTimeElementRequiredDefault(TEST_TIME)
            .localDateTimeElementDefault(TEST_TIME.toLocalDateTime())
            .localDateTimeElementRequiredDefault(TEST_TIME.toLocalDateTime())
            .build();
    }

    @Test
    void xmlNoDefaults() {
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> buildNoDefaults().writeSelfTo(out)), "expected_time_no_defaults.xml");
    }

    @Test
    void xmlWithDefaults() {
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> buildWithDefaults().writeSelfTo(out)), "expected_time_with_defaults.xml");
    }
}

package io.github.cbarlin.aru.tests.c_odd_types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

class BooleanTest {

    BooleanBag buildNoDefaults() {
        return BooleanBagUtils.builder()
            .primitiveBooleanAttribute(true)
            .boxedBooleanAttribute(Boolean.TRUE)
            .boxedBooleanAttributeRequired(Boolean.TRUE)
            .primitiveBooleanElement(true)
            .boxedBooleanElement(Boolean.TRUE)
            .boxedBooleanElementRequired(Boolean.TRUE)
            .build();
    }

    BooleanBag buildWithDefaults() {
        return buildNoDefaults().with()
            .boxedBooleanElementDefault(Boolean.TRUE)
            .boxedBooleanElementRequiredDefault(Boolean.TRUE)
            .build();
    }

    @Test
    void xmlNoDefaults() {
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> buildNoDefaults().writeSelfTo(out)), "expected_bools_no_defaults.xml");
    }

    @Test
    void xmlWithDefaults() {
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> buildWithDefaults().writeSelfTo(out)), "expected_bools_with_defaults.xml");
    }

    @Test
    void diffWhenNoChanges() {
        final var original = buildWithDefaults();
        final var updated = buildWithDefaults();

        final var diff = original.diff(updated);

        assertFalse(diff.hasChanged());
    }

    @Test
    void diffWhenSomethingChanges() {
        final var original = buildWithDefaults();
        final var updated = buildWithDefaults().withBoxedBooleanElementRequired(Boolean.FALSE);

        final var diff = original.diff(updated);

        assertTrue(diff.hasChanged());
        assertTrue(diff.hasBoxedBooleanElementRequiredChanged());
        assertFalse(diff.hasPrimitiveBooleanElementChanged());
    }
}

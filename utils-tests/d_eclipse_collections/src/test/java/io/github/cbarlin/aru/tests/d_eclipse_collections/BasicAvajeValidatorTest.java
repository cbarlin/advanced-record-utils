package io.github.cbarlin.aru.tests.d_eclipse_collections;

import io.avaje.validation.ConstraintViolationException;
import io.avaje.validation.Validator;
import io.avaje.validation.groups.Default;
import jakarta.xml.bind.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicAvajeValidatorTest {

    @Test
    void validBuild() {
        final String expected = "This has a value";
        final SomeImplAUtils.Builder builder = SomeImplAUtils.builder()
            .field(expected);

        final SomeImplA a = assertDoesNotThrow(() -> builder.build(), "Original build should pass");
        assertEquals(expected, a.field());
        final SomeImplA b = assertDoesNotThrow(() -> builder.buildAndValidate(), "Build and validate should allow the item to be built");
        assertEquals(expected, b.field());
        final Validator validator = Validator.builder()
            .setDefaultLocale(Locale.ENGLISH)
            .build();
        final SomeImplA c = assertDoesNotThrow(() -> builder.build(validator), "Build with a validator should pass");
        assertEquals(expected, c.field());
        final SomeImplA d = assertDoesNotThrow(() -> builder.build(validator, SomeImplA.class), "Build with a validator should pass");
        assertEquals(expected, d.field());
        final SomeImplA e = assertDoesNotThrow(() -> builder.buildAndValidate(SomeImplA.class), "Build with a validator should pass");
        assertEquals(expected, e.field());
    }

    @Test
    void unsetField() {
        final SomeImplAUtils.Builder builder = SomeImplAUtils.builder();

        final SomeImplA a = assertDoesNotThrow(() -> builder.build(), "Original build should pass");
        assertNull(a.field());

        final String message = "A null field should not be valid";
        assertThrows(ConstraintViolationException.class, builder::buildAndValidate, message);
        assertThrows(ConstraintViolationException.class, () -> builder.buildAndValidate(Default.class), message);
        final Validator validator = Validator.builder()
            .setDefaultLocale(Locale.ENGLISH)
            .build();
        assertThrows(ConstraintViolationException.class, () -> builder.build(validator), message);
        assertThrows(ConstraintViolationException.class, () -> builder.build(validator, Default.class), message);
    }

    @Test
    void emptyString() {
        final SomeImplAUtils.Builder builder = SomeImplAUtils.builder()
            .field("");

        final SomeImplA a = assertDoesNotThrow(() -> builder.build(), "Original build should pass");
        assertEquals("", a.field());

        final String message = "An empty string should not be valid";
        assertThrows(ConstraintViolationException.class, builder::buildAndValidate, message);
        assertThrows(ConstraintViolationException.class, () -> builder.buildAndValidate(Default.class), message);
        final Validator validator = Validator.builder()
            .setDefaultLocale(Locale.ENGLISH)
            .build();
        assertThrows(ConstraintViolationException.class, () -> builder.build(validator), message);
        assertThrows(ConstraintViolationException.class, () -> builder.build(validator, Default.class), message);
    }

    @Test
    void blankString() {
        final SomeImplAUtils.Builder builder = SomeImplAUtils.builder()
            .field("              ");

        final SomeImplA a = assertDoesNotThrow(() -> builder.build(), "Original build should pass");
        assertEquals("              ", a.field());

        final String message = "An blank string should not be valid";
        assertThrows(ConstraintViolationException.class, builder::buildAndValidate, message);
        assertThrows(ConstraintViolationException.class, () -> builder.buildAndValidate(Default.class), message);
        final Validator validator = Validator.builder()
            .setDefaultLocale(Locale.ENGLISH)
            .build();
        assertThrows(ConstraintViolationException.class, () -> builder.build(validator), message);
        assertThrows(ConstraintViolationException.class, () -> builder.build(validator, Default.class), message);
    }
}

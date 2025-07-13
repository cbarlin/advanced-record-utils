package io.github.cbarlin.aru.tests.c_odd_types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

class TypesTests {

    @Test
    void builder() {
        final OddTypeBag someRecord = assertDoesNotThrow(() -> {
            return OddTypeBagUtils.builder()
                .listOfItems(
                    List.of(
                        "Turning Random Internet Drama into Songs Part 4 with Lubalin and Alison Brie",
                        "What did Caroline do Helen?"
                    )
                )
                .optionalDouble(2.2)
                .someOptionalInt(69)
                .someOptionalLong(12345678987654321l)
                .build();
        });

        assertEquals("This is horse isn't from here", someRecord.thisShouldNotBeInTheBuilder());

        assertThrows(NoSuchMethodException.class, () -> {
            OddTypeBagUtils.Builder.class.getDeclaredMethod("thisShouldNotBeInTheBuilder");
        }, "Method 'thisShouldNotBeInTheBuilder' should not be declared on the builder.");

        assertThrows(NoSuchMethodException.class, () -> {
            OddTypeBagUtils.Builder.class.getDeclaredMethod("thisShouldNotBeInTheBuilder", String.class);
        }, "Method 'thisShouldNotBeInTheBuilder' should not be declared on the builder.");
    }

    @Test
    void wither() {
        final OddTypeBag someRecord = assertDoesNotThrow(() -> {
            return OddTypeBagUtils.builder()
                .listOfItems(
                    List.of(
                        "Turning Random Internet Drama into Songs Part 4 with Lubalin and Alison Brie",
                        "What did Caroline do Helen?"
                    )
                )
                .optionalDouble(2.2)
                .someOptionalInt(69)
                .someOptionalLong(12345678987654321l)
                .build();
        });

        final OddTypeBag changed = someRecord.withOptionalDouble(420.0)
            .withSomeOptionalInt(42)
            .withSomeOptionalLong(9999999l);

        assertEquals("This is horse isn't from here", changed.thisShouldNotBeInTheBuilder());

        assertThrows(NoSuchMethodException.class, () -> {
            OddTypeBagUtils.With.class.getDeclaredMethod("thisShouldNotBeInTheBuilder");
        }, "Method 'thisShouldNotBeInTheBuilder' should not be declared on the builder.");

        assertThrows(NoSuchMethodException.class, () -> {
            OddTypeBagUtils.With.class.getDeclaredMethod("thisShouldNotBeInTheBuilder", String.class);
        }, "Method 'thisShouldNotBeInTheBuilder' should not be declared on the builder.");
    }

    @Test
    void merger() {
        final OddTypeBag objA = OddTypeBagUtils.builder()
            .optionalDouble(15.0)
            .someOptionalInt(69)
            .build();

        final OddTypeBag objB = OddTypeBagUtils.builder()
            .someOptionalLong(420)
            .someOptionalInt(13)
            .build();
        
        final OddTypeBag merged = objA.merge(objB);

        assertFalse(objB.optionalDouble().isPresent(), "Optional Double overwrote on other obj");
        assertTrue(merged.optionalDouble().isPresent(), "Optional Double didn't merge across");
        assertEquals(15.0, merged.optionalDouble().getAsDouble());

        assertFalse(objA.someOptionalLong().isPresent(), "Optional Long overwrote on other obj");
        assertTrue(merged.someOptionalLong().isPresent(), "Optional Long didn't merge across");
        assertEquals(420l, merged.someOptionalLong().getAsLong());

        assertEquals(69, objA.someOptionalInt().getAsInt());
        assertEquals(13, objB.someOptionalInt().getAsInt());
        assertEquals(69, merged.someOptionalInt().getAsInt());
    }

    @Test
    void differ() {
        final OddTypeBag objA = OddTypeBagUtils.builder()
            .optionalDouble(15.0)
            .someOptionalInt(69)
            .build();

        final OddTypeBag objB = OddTypeBagUtils.builder()
            .someOptionalLong(420)
            .someOptionalInt(69)
            .build();

        final var diff = objA.diff(objB);

        assertTrue(diff.hasChanged());
        assertFalse(diff.hasListOfItemsChanged());
        assertFalse(diff.hasMoreOptionalIntsChanged());
        assertTrue(diff.hasSomeOptionalLongChanged());
        assertFalse(diff.hasSomeOptionalIntChanged());
    }
    
    @Test
    void xml() {
        final OddTypeBag someRecord = assertDoesNotThrow(() -> {
            return OddTypeBagUtils.builder()
                .listOfItems(
                    List.of(
                        "Turning Random Internet Drama into Songs Part 4 with Lubalin and Alison Brie",
                        "What did Caroline do Helen?"
                    )
                )
                .optionalDouble(2.2)
                .someOptionalInt(69)
                .someOptionalLong(12345678987654321l)
                .addMoreOptionalInts(OptionalInt.of(42))
                .addMoreOptionalInts(OptionalInt.of(84))
                .id(UUID.fromString("11f42990-d9ad-42ac-998d-74e2243b01a2"))
                .build();
        });

        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out)), "expected_odd_type_bag.xml");
    }
}

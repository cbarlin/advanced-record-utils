package io.github.cbarlin.aru.tests.c_odd_types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
                .build();
        });

        final String xmlString = assertDoesNotThrow(() -> ConvertToXml.convertToXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out))));
        assertEquals(
            //language=XML
            "<?xml version=\"1.0\" ?><OddTypeBag><WrapperOfLSO><ListOfItems>Turning Random Internet Drama into Songs Part 4 with Lubalin and Alison Brie</ListOfItems><ListOfItems>What did Caroline do Helen?</ListOfItems></WrapperOfLSO><SomeOptionalInt>69</SomeOptionalInt><SomeOptionalLong>12345678987654321</SomeOptionalLong><OptionalDouble>2.2</OptionalDouble><ThisShouldNotBeInTheBuilder>This is horse isn't from here</ThisShouldNotBeInTheBuilder></OddTypeBag>", 
            xmlString
        );

    }
}

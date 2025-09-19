package io.github.cbarlin.aru.tests.d_eclipse_collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyTest {

    @Test
    void dependencyTest() {
        final DependsOnRecord dor = assertDoesNotThrow(
            () -> DependsOnRecordUtils.builder()
                .addImmutableListOfA(aBuilder -> aBuilder.someIntField(32))
                .addMutableListOfA(aBuilder -> aBuilder.someStringField("woot"))
                .addImmutableSetOfA(aBuilder -> aBuilder.someIntField(42))
                .addImmutableSetOfA(aBuilder -> aBuilder.someIntField(42))
                .build()
        );
        assertEquals(1, dor.immutableSetOfA().size());
    }
}

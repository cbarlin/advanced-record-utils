package io.github.cbarlin.aru.tests.d_eclipse_collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicMapStructTest {

    @Test
    void canSwap() {
        final SomeImplA a = SomeImplAUtils.builder()
            .field("This is a value")
            .build();
        final SomeImplB b = SomeImplMapper.INSTANCE.fromA(a);
        assertEquals(a.field(), b.anotherField());
        // This proves that MapStruct has used the builder, as otherwise it'll
        //   pass '0' to the canonical constructor
        assertEquals(42, b.iAmNotNeeded(), "ARU Builder was not used");
    }
}

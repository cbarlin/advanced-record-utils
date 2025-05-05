package io.github.cbarlin.aru.tests.b_types_alias;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WitherTests {

    void canHandleReplacementWithDelaias() {
        final SomeRecord original = SomeRecordUtils.builder()
            .randomIntA(69)
            .build();
        final SomeRecord after = original.withRandomIntA(42);

        assertEquals(69, original.randomIntA().value());
        assertEquals(42, after.randomIntA().value());
    }
}

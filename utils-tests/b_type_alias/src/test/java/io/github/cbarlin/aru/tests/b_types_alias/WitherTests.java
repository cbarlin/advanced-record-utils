package io.github.cbarlin.aru.tests.b_types_alias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class WitherTests {

    @Test
    void canHandleReplacementWithDealias() {
        final SomeRecord original = SomeRecordUtils.builder()
            .randomIntA(69)
            .bookName("This is a bookname")
            .authorName("This is the author name")
            .randomIntB(13)
            .build();
        final SomeRecord after = original.withRandomIntA(42)
            .withBookName((BookName) null);

        assertEquals(69, original.randomIntA().value());
        assertEquals(42, after.randomIntA().value());
        assertNull(after.bookName());
        assertEquals(original.authorName(), after.authorName());
        assertEquals(original.randomIntB(), after.randomIntB());
    }
}

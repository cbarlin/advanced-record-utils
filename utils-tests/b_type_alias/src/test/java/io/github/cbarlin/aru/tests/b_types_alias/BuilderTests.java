package io.github.cbarlin.aru.tests.b_types_alias;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BuilderTests {

    @Test
    void testCanBuildByBothAliasAndNonaliasedType() {
        final SomeRecord someRecord = SomeRecordUtils.builder()
            .authorName("This is an author")
            .bookName(new BookName("And this is a book"))
            .randomIntA(42)
            .randomIntB(new RandomIntB(69))
            .randomIntC(13)
            .build();
        assertEquals("This is an author", someRecord.authorName().value());
        assertEquals(new RandomIntA(42), someRecord.randomIntA());
    }
}

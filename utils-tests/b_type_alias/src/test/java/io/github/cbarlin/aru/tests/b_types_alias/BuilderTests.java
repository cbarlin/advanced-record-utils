package io.github.cbarlin.aru.tests.b_types_alias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals("And this is a book", someRecord.bookName().value());
        assertEquals(69, someRecord.randomIntB().value());
        assertEquals(13, someRecord.randomIntC().value());
    }
    
    @Test
    void testNullChanges() {
        final SomeRecord withSwap = SomeRecordUtils.builder()
            .authorName("Test")
            .authorName( (AuthorName) null)
            .build();
        final OtherRecord withoutSwap = OtherRecordUtils.builder()
            .authorName("Test")
            .authorName( (AuthorName) null)
            .build();

        assertNull(withSwap.randomIntA());
        assertNull(withSwap.authorName());
        assertNull(withoutSwap.randomIntA());
        assertNotNull(withoutSwap.authorName());
    }
}

package io.github.cbarlin.aru.tests.b_types_alias;

import io.github.cbarlin.aru.tests.a_core_dependency.AnEnumInDep;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        // Test both that we have constructions of aliases...
        assertEquals(new RandomIntA(42), someRecord.randomIntA());
        // ... and that we can dealias them...
        assertEquals("This is an author", someRecord.authorName().value());        
        assertEquals("And this is a book", someRecord.bookName().value());
        assertEquals(69, someRecord.randomIntB().value());
        assertEquals(13, someRecord.randomIntC().value());

        assertThrows(NoSuchMethodException.class, () -> {
            SomeRecordUtils.Builder.class.getDeclaredMethod("another");
        }, "Method 'another' should not be declared on the builder.");

        assertThrows(NoSuchMethodException.class, () -> {
            SomeRecordUtils.Builder.class.getDeclaredMethod("another", String.class);
        }, "Method 'another' should not be declared on the builder.");
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

    @Test
    void testCrossCompilationTypeConverterDetection() {
        // The "AnEnumInDep" from String converter should be loaded via the
        //   request to load MyRecordBUtils, which should have the converter
        //   from when it was found in the "AnEnumInDep" class itself
        final SomeRecord withEnumMonday = SomeRecordUtils.builder()
            .anEnumInDep("Monday")
            .build();
        final SomeRecord withEnumTuesday = SomeRecordUtils.builder()
            .anEnumInDep("Tuesday")
            .build();
        final SomeRecord withEnumOther = SomeRecordUtils.builder()
            .anEnumInDep("Some other string")
            .build();
        assertEquals(AnEnumInDep.MONDAY, withEnumMonday.anEnumInDep());
        assertEquals(AnEnumInDep.TUESDAY, withEnumTuesday.anEnumInDep());
        assertEquals(AnEnumInDep.TUESDAY, withEnumOther.anEnumInDep());
    }
}

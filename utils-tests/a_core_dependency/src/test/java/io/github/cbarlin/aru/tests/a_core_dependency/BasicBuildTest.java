package io.github.cbarlin.aru.tests.a_core_dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BasicBuildTest {

    @Test
    void fluentBuild() {
        final SomeInterface someInterface = MyRecordCUtils.builder()
            .bItem(bBuilder -> {
                bBuilder.otherItem(other -> other.someIntField(42).someStringField("testing"))
                    .woo(woo -> woo.someIntField(15))
                    .recursionFtw(recursion -> {
                        recursion.otherItem(blah -> blah.someIntField(69));
                    });
            })
            .build();
        assertEquals(42, someInterface.bItem().otherItem().someIntField());
        assertEquals(15, someInterface.bItem().woo().someIntField());
        assertEquals(69, someInterface.bItem().recursionFtw().otherItem().someIntField());
        assertEquals("testing", someInterface.bItem().otherItem().someStringField());
        assertNull(someInterface.bItem().recursionFtw().recursionFtw());
        assertNull(someInterface.bItem().recursionFtw().woo());
        assertNull(someInterface.bItem().recursionFtw().otherItem().someStringField());
        assertNull(someInterface.bItem().woo().someStringField());
    }

    @Test
    void copyBuild() {
        final MyRecordB firstRound = MyRecordBUtils.builder()
            .otherItem(oth -> oth.someIntField(42))
            .otherItem(oth -> oth.someIntField(69))
            .build();
        final MyRecordB secondRound = MyRecordBUtils.builder(firstRound)
            .otherItem(oth -> oth.someIntField(420))
            .build();
        assertEquals(420, secondRound.otherItem().someIntField());
    }
}

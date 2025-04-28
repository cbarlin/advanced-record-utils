package io.github.cbarlin.aru.tests.a_core_dependent;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.a_core_dependent.MyRecordFOptionA;
import io.github.cbarlin.aru.tests.a_core_dependent.MyRecordG;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Condition;

class BasicTests {

    @Test
    void worksAcrossLibraryBounds() {
        final MyRecordG recG = MyRecordGUtils.builder()
            .addMoreAItems(aB -> aB.someIntField(42))
            .addMoreAItems(aB -> aB.someIntField(69))
            .addListOfSealed(
                MyRecordFOptionAUtils.builder()
                    .myRecordE(e -> e.myRecordD(d -> d.theInterface(b -> b.otherItem(a -> a.someIntField(420)))))
                    .build()
            )
            .build();

        assertThat(recG.moreAItems())
            .hasSize(2)
            .areExactly(1, new Condition<>(recA -> recA.someIntField() == 42, "Has a 42 entry"))
            .areExactly(1, new Condition<>(recA -> recA.someIntField() == 69, "Has a 69 entry"));

        assertThat(recG.listOfSealed())
            .hasSize(1)
            .hasOnlyElementsOfType(MyRecordFOptionA.class);
    }
}

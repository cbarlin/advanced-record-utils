package io.github.cbarlin.aru.tests.a_core_dependent;

import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordAUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.assertj.core.api.Condition;

class BasicTests {

    @Test
    void worksAcrossLibraryBounds() {
        final MyRecordG recG = MyRecordGUtils.builder()
            .addMoreAItems(aB -> aB.someIntField(42))
            .addMoreAItems(aB -> aB.someIntField(69))
            .addListOfSealed(
                MyRecordFOptionAUtils.builder()
                    .myRecordE(e -> e.someOtherValue("Hi!").myRecordD(d -> d.theInterface(b -> b.otherItem(a -> a.someIntField(420)))))
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

    @Test
    void optionBFound() {
        final MyRecordFOptionB optionB = MyRecordFOptionBUtils.builder()
            .myRecordE(eBuilder -> eBuilder.someOtherValue("woot"))
            .build();
        assertThat(optionB.myRecordE())
            .isNotNull();
    }

    @Test
    void beforeBuildCall() {
        final MyRecordG versionA = MyRecordGUtils.builder()
             .build();
        assertThat(versionA.appendMe())
            .isNotNull()
            .isEqualTo("Nothing here!");
        final MyRecordG versionB = MyRecordGUtils.builder()
            .appendMe("lol")
            .build();
        assertThat(versionB.appendMe())
            .isNotNull()
            .isEqualTo("lol and me!");
    }

    @Test
    void extensionMethods() {
        assertInstanceOf(GeeBuilder.class, MyRecordGUtils.builder());
        final MyRecordGUtils.Builder builder = MyRecordGUtils.builder();
        builder.sealedExt(MyRecordFOptionAUtils.builder().build());
        assertNotNull(builder.theSealedOne());
        assertThat(builder.listOfSealed())
            .isNotNull()
            .hasSize(1);

        assertTrue(builder.aItemExt(MyRecordAUtils.builder().someIntField(3).build()));
        assertNotNull(builder.anA());
        assertThat(builder.moreAItems())
            .isNotNull()
            .hasSize(1);
        assertFalse(builder.aItemExt(MyRecordAUtils.builder().someIntField(3).build()));
        assertTrue(builder.aItemExt(MyRecordAUtils.builder().someIntField(5).build()));
        assertNotNull(builder.anA());
        assertEquals(5, builder.anA().someIntField());
        assertThat(builder.moreAItems())
            .isNotNull()
            .hasSize(2);
    }
}

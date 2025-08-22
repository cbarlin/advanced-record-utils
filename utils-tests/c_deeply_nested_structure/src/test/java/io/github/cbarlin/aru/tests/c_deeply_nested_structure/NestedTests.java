package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import io.avaje.jsonb.Jsonb;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordCUtils;
import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NestedTests {

    @Test
    void basicTest() {
        final RootItem rootItemA = RootItemUtils.builder()
            .addFirstLevels(
                first -> first.recurringReference(
                    recurring -> recurring.itemA("A")
                        .itemB("B")
                        .itemC("C")
                        .addRecurisveItems(
                            recursiveA -> recursiveA.notRecursiveA("D")
                                .notRecursiveB("E")
                                .notRecursiveC("F")
                                .addRecursionFtw(
                                    recursiveB -> recursiveB.notRecursiveA("G")
                                    
                                )
                        )
                )
                .secondLevelA(
                    secondA -> secondA.someValueAsThirdLevelAFromA(
                        someValue -> someValue.thirdString("This is a string!")
                    )
                )
            )
            .anotherField(42)
            // This call will be replaced by the one below it, but
            //   it will prove that the settings of the package and the
            //   root item have been merged (as one defines "update" and the other "ToCurrent")
            .updateTestOdtElToCurrent()
            .testOdtEl(OffsetDateTime.of(2025, 7, 01, 18, 15, 42, 0, ZoneOffset.ofHours(10)))
            .make();
        
        final RootItem rootItemB = RootItemUtils.builder()
            .anotherField(69)
            .yetAnotherField("I should be in the output!")
            .addFirstLevels(
                first -> first.secondLevelB(
                    secondB -> secondB.thirdLevelAFromB(
                        t -> t.fourthLevelA(
                            four -> four.letsGoFive(
                                five -> five.nowToSix(
                                    six -> six.woo(
                                        seven -> seven
                                            // This is set to null (proving the method exists)
                                            //  and then immediately overridden.
                                            // The override is tested via the XML check
                                            .setAndImDoneToNull()
                                            .andImDone(
                                                recurringAgain -> recurringAgain.itemA("Hi!")
                                            )
                                    )
                                )
                            )
                            .oohNotLinear(
                                sevenAgain -> sevenAgain.andImDone(
                                    areWeDone -> areWeDone.itemB("Probs not")
                                )
                            )
                        )
                    )
                )
                .secondLevelC(
                    secondC -> secondC.endOfTheLineHere("Nice")
                )
            )
            .testOdtAttr(OffsetDateTime.of(2025, 01, 01, 18, 15, 42, 0, ZoneOffset.ofHours(10)))
            .make();
        
        final RootItem merged = rootItemA.merge(rootItemB);

        assertEquals("I should be in the output!", merged.yetAnotherField());
        assertEquals(42, merged.anotherField());
        assertThat(merged.firstLevels())
            .hasSize(2)
            .doesNotContainNull();
            
        // OK, time to convert to XML
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> merged.writeSelfTo(out)), "expected_b.xml");

        Jsonb jsonb = Jsonb.builder().build();
        final String result = assertDoesNotThrow(() -> jsonb.toJson(merged));
        assertThat(result)
            .isNotNull()
            .isNotBlank();
    }

    @Test
    void setXToNullMethodDetection() {
        var m = assertDoesNotThrow(
            () -> SeventhLevelAUtils.Builder.class.getDeclaredMethod("setAndImDoneToNull")
        );
        assertNotNull(m);
        // This shouldn't be generated because the component is a primitive
        assertThrows(NoSuchMethodException.class, () -> {
            ThirdLevelBFromAUtils.Builder.class.getDeclaredMethod("setSomeValueToNull");
        }, "Method 'setSomeValueToNull' should not be declared on the builder.");
    }

    @Test
    void defValueSet() {
        final RootItem someRecord = RootItemUtils.builder()
            .testDefault("NotThis!")
            .yetAnotherField("I am required!")
            .make();

        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out)), "expected_a.xml");
    }

    @Test
    void notSetRequired() throws IOException, XMLStreamException {
        final RootItem someRecord = RootItemUtils.builder()
            .testDefault("NotThis!")
            .make();
        
        ConvertToXml.convertToXml(out -> assertThrows(IllegalArgumentException.class, () -> someRecord.writeSelfTo(out)));
    }
}

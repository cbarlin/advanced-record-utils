package io.github.cbarlin.aru.tests.cdncl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;
import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

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
            .testOdtEl(OffsetDateTime.of(2025, 7, 01, 18, 15, 42, 0, ZoneOffset.ofHours(10)))
            .build();
        
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
                                        seven -> seven.andImDone(
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
            .build();
        
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
    void javaImmutableCollectionByDefault() {
        final RootItem someRecord = RootItemUtils.builder()
            .addFirstLevels(lvl -> {})
            .build();

        final FirstLevel toAdd = FirstLevelUtils.builder()
            .build();
        final var list = someRecord.firstLevels();
        
        assertThrows(UnsupportedOperationException.class, () -> list.add(toAdd));
    }

    @Test
    void defValueSet() {
        final RootItem someRecord = RootItemUtils.builder()
            .testDefault("NotThis!")
            .yetAnotherField("I am required!")
            .build();

        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out)), "expected_a.xml");
    }

    @Test
    void notSetRequired() throws IOException, XMLStreamException {
        final RootItem someRecord = RootItemUtils.builder()
            .testDefault("NotThis!")
            .build();
        
        ConvertToXml.convertToXml(out -> assertThrows(NullPointerException.class, () -> someRecord.writeSelfTo(out)));
    }

    @Test
    void useTypeConverter() {
        final FirstLevel fl = assertDoesNotThrow(() -> FirstLevelUtils.builder()
            .recurringReference("AAA", "BBB")
            .build());
        assertNotNull(fl.recurringReference().recurisveItems());
    }

    @Test
    void testAddFluentSetterFromOptionalInterface() {
        final SecondLevelA result = SecondLevelAUtils.builder()
                .optIfaceAsThirdLevelAFromA(b -> b.thirdString("A"))
                .optIfaceAsThirdLevelAFromA(b -> b.thirdString("AAAA"))
                .build();
        assertThat(result.optIface())
                .isPresent()
                .containsInstanceOf(ThirdLevelAFromA.class)
                .get()
                .hasFieldOrPropertyWithValue("thirdString", "AAAA");
    }
}

package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals; 

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

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
        final StringBuilder xmlStringBuilder = new StringBuilder();
        assertDoesNotThrow(() -> {
            final XMLOutputFactory factory = XMLOutputFactory.newFactory();
            factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
            try (
                final StringBuilderWriter stringBuilderWriter = new StringBuilderWriter(xmlStringBuilder);
                final WriterOutputStream ws = WriterOutputStream.builder()
                    .setWriter(stringBuilderWriter)
                    .setCharset(StandardCharsets.UTF_8)
                    .get();
            ) {
                final XMLStreamWriter streamWriter = factory.createXMLStreamWriter(ws, StandardCharsets.UTF_8.name());
                streamWriter.writeStartDocument();
                merged.writeSelfTo(streamWriter);
                streamWriter.writeEndDocument();
                // Flush and close the XML streams beacause apparently xml doesn't have autocloseable...
                streamWriter.flush();
                streamWriter.close();
                ws.flush();
                stringBuilderWriter.flush();
            }
        });
        final String xmlString = xmlStringBuilder.toString();
        assertEquals("<?xml version=\"1.0\" ?><RootItem xmlns:wooo=\"ns://namedA\" xmlns:yayyyyy=\"ns://namedB\" xmlns=\"ns://nxA\" butIHaveAnotherName=\"I should be in the output!\" anotherField=\"42\" testOdtAttr=\"2025-01-01T08:15:42Z\"><FirstLevels><recurringReference itemA=\"A\" itemB=\"B\" itemC=\"C\"><recurisveItems notRecursiveB=\"E\" notRecursiveC=\"F\"><notRecursiveA>D</notRecursiveA><recursionFtw><notRecursiveA>G</notRecursiveA></recursionFtw></recurisveItems></recurringReference><secondLevelA><ThirdLevelAFromA><thirdString>This is a string!</thirdString></ThirdLevelAFromA></secondLevelA></FirstLevels><FirstLevels><secondLevelB><thirdLevelAFromB><fourthLevelA><letsGoFive><nowToSix><woo><andImDone itemA=\"Hi!\"></andImDone></woo></nowToSix></letsGoFive><oohNotLinear><andImDone itemB=\"Probs not\"></andImDone></oohNotLinear></fourthLevelA></thirdLevelAFromB></secondLevelB><secondLevelC><endOfTheLineHere>Nice</endOfTheLineHere></secondLevelC></FirstLevels><testOdtEl>2025-07-01T08:15:42Z</testOdtEl></RootItem>", xmlString);
    }

}

package io.github.cbarlin.aru.tests.d_eclipse_collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

class XmlRecordTest {
    @Test
    void serialTest() {
        final CanXmlTheRecord someRecord = CanXmlTheRecordUtils.builder()
            .someString("ItemA")
            .addSomeItemsAsElements("ThisIsAnElement")
            .addSomeItemsAsElements("AndThisIsTheSecond")
            .addAnotherObject(
                b -> b.addSomeItemsAsElements("ThisIsAThirdButItsInside")
                    .someString("ItemB")
            )
            .addSomeItemsAsElements("And this is the forth, but it will show as the 3rd")
            .addSomeItemsAsElements("Let's have some fun characters: オルフェウス")
            .addSomeItemsAsElements("!@#$%^%&*DF^D&S^&*^$%$ << [[]] >")
            .addSomeItemsAsElements("Awesomes")
            .build();
        
        final String xmlString = assertDoesNotThrow(() -> ConvertToXml.convertToXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out))));
        assertEquals(
            "<?xml version=\"1.0\" ?><CanXmlTheRecord SomeString=\"ItemA\"><WrapMe><SelfReflection SomeString=\"ItemB\"><SomeElementStrings>ThisIsAThirdButItsInside</SomeElementStrings></SelfReflection></WrapMe><SomeElementStrings>ThisIsAnElement</SomeElementStrings><SomeElementStrings>AndThisIsTheSecond</SomeElementStrings><SomeElementStrings>And this is the forth, but it will show as the 3rd</SomeElementStrings><SomeElementStrings>Let's have some fun characters: オルフェウス</SomeElementStrings><SomeElementStrings>!@#$%^%&amp;*DF^D&amp;S^&amp;*^$%$ &lt;&lt; [[]] &gt;</SomeElementStrings><SomeElementStrings>Awesomes</SomeElementStrings></CanXmlTheRecord>",
            xmlString
        );
    }
}

package io.github.cbarlin.aru.tests.d_eclipse_collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

import java.util.List;

class XmlRecordTest {
    @Test
    void serialTest() {
        final CanXmlTheRecord someRecord = CanXmlTheRecordUtils.builder()
            .someString("ItemA")
            .addSomeItemsAsElements("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
            .removeSomeItemsAsElements("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
            .addSomeItemsAsElements("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
            .removeSomeItemsAsElements(a -> true)
            .addSomeItemsAsElements("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
            .addSomeItemsAsElements("ThisIsAnElement")
            .addSomeItemsAsElements("AndThisIsTheSecond")
            .retainAllSomeItemsAsElements(List.of("AndThisIsTheSecond", "ThisIsAnElement", "I'm not in the collection and I shouldn't end up in it!"))
            .addAnotherObject(
                b -> b.addSomeItemsAsElements("ThisIsAThirdButItsInside")
                    .someString("ItemB")
            )
            .addSomeItemsAsElements("And this is the forth, but it will show as the 3rd")
            .addSomeItemsAsElements("Let's have some fun characters: オルフェウス")
            .addSomeItemsAsElements("!@#$%^%&*DF^D&S^&*^$%$ << [[]] >")
            .addSomeItemsAsElements("Awesomes")
            .build();
        
        ConvertToXml.compareXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out)), "expected_eclipse.xml");
    }
}

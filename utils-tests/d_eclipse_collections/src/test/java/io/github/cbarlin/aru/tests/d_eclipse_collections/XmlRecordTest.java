package io.github.cbarlin.aru.tests.d_eclipse_collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Test;

class XmlRecordTest {
    @Test
    void serialTest() {
        final CanXmlTheRecord attemptXml = CanXmlTheRecordUtils.builder()
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
                attemptXml.writeSelfTo(streamWriter);
                streamWriter.writeEndDocument();
                // Flush and close the XML streams beacause apparently xml doesn't have autocloseable...
                streamWriter.flush();
                streamWriter.close();
                ws.flush();
                stringBuilderWriter.flush();
            }
        });
        final String xmlString = xmlStringBuilder.toString();
        assertEquals(
            "<?xml version=\"1.0\" ?><CanXmlTheRecord SomeString=\"ItemA\"><WrapMe><SelfReflection SomeString=\"ItemB\"><SomeElementStrings>ThisIsAThirdButItsInside</SomeElementStrings></SelfReflection></WrapMe><SomeElementStrings>ThisIsAnElement</SomeElementStrings><SomeElementStrings>AndThisIsTheSecond</SomeElementStrings><SomeElementStrings>And this is the forth, but it will show as the 3rd</SomeElementStrings><SomeElementStrings>Let's have some fun characters: オルフェウス</SomeElementStrings><SomeElementStrings>!@#$%^%&amp;*DF^D&amp;S^&amp;*^$%$ &lt;&lt; [[]] &gt;</SomeElementStrings><SomeElementStrings>Awesomes</SomeElementStrings></CanXmlTheRecord>",
            xmlString
        );
    }
}

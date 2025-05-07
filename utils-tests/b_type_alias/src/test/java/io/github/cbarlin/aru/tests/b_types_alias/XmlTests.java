package io.github.cbarlin.aru.tests.b_types_alias;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Test;

class XmlTests {

    @Test
    void canConvertToXml() {
        final SomeRecord someRecord = SomeRecordUtils.builder()
            .authorName("This is an author")
            .bookName(new BookName("And this is a book"))
            .randomIntA(42)
            .randomIntB(new RandomIntB(69))
            .randomIntC(13)
            .build();
            
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
                someRecord.writeSelfTo(streamWriter);
                streamWriter.writeEndDocument();
                // Flush and close the XML streams beacause apparently xml doesn't have autocloseable...
                streamWriter.flush();
                streamWriter.close();
                ws.flush();
                stringBuilderWriter.flush();
            }
        });
        final String xmlString = xmlStringBuilder.toString();
        assertEquals("<?xml version=\"1.0\" ?><SomeTest AuthorName=\"This is an author\" one=\"42\" B=\"69\" C=\"13\"><BookName>And this is a book</BookName></SomeTest>", xmlString);

    }
}

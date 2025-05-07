package io.github.cbarlin.aru.tests.b_infer_xml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Test;

class XmlTest {
    @Test
    void matchingName() {
        final InferMatchingName someRecord = InferMatchingNameUtils.builder()
            .iShouldBeAnElement("and it is")
            .iShouldBeIgnored("Do I appear?")
            .randomAttribute(42)
            .someDefinedObject("This is manual")
            .soShouldI(OffsetDateTime.of(2025, 01, 02, 03, 04, 05, 06, ZoneOffset.UTC))
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
        assertEquals("<?xml version=\"1.0\" ?><InferMatchingName randomAttribute=\"42\"><iShouldBeAnElement>and it is</iShouldBeAnElement><soShouldI>2025-01-02T03:04:05.000000006Z</soShouldI><ThisIsTheManualOne>This is manual</ThisIsTheManualOne></InferMatchingName>", xmlString);

    }

    @Test
    void upperName() {
        final InferUpperCamelName someRecord = InferUpperCamelNameUtils.builder()
            .iShouldBeAnElement("and it is")
            .iShouldBeIgnored("Do I appear?")
            .randomAttribute(42)
            .someDefinedObject("This is manual")
            .soShouldI(OffsetDateTime.of(2025, 01, 02, 03, 04, 05, 06, ZoneOffset.UTC))
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
        assertEquals("<?xml version=\"1.0\" ?><InferUpperCamelName randomAttribute=\"42\"><IShouldBeAnElement>and it is</IShouldBeAnElement><SoShouldI>2025-01-02T03:04:05.000000006Z</SoShouldI><ThisIsTheManualOne>This is manual</ThisIsTheManualOne></InferUpperCamelName>", xmlString);

    }
}

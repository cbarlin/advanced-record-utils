package io.github.cbarlin.aru.tests.b_infer_xml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

class XmlTest {

    private static final OffsetDateTime TEST_DATE = OffsetDateTime.of(2025, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC);

    @Test
    void matchingName() {
        final InferMatchingName someRecord = InferMatchingNameUtils.builder()
            .iShouldBeAnElement("and it is")
            .iShouldBeIgnored("Do I appear?")
            .randomAttribute(42)
            .someDefinedObject("This is manual")
            .soShouldI(TEST_DATE)
            .build();
            
        // OK, time to convert to XML
        final String xmlString = assertDoesNotThrow(() -> ConvertToXml.convertToXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out))));
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><InferMatchingName randomAttribute=\"42\"><iShouldBeAnElement>and it is</iShouldBeAnElement><soShouldI>2025-01-02T03:04:05.000000006Z</soShouldI><ThisIsTheManualOne>This is manual</ThisIsTheManualOne></InferMatchingName>", xmlString);

    }

    @Test
    void upperName() {
        final InferUpperCamelName someRecord = InferUpperCamelNameUtils.builder()
            .iShouldBeAnElement("and it is")
            .iShouldBeIgnored("Do I appear?")
            .randomAttribute(42)
            .someDefinedObject("This is manual")
            .soShouldI(TEST_DATE)
            .build();
            
        final String xmlString = assertDoesNotThrow(() -> ConvertToXml.convertToXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out))));
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><InferUpperCamelName randomAttribute=\"42\"><IShouldBeAnElement>and it is</IShouldBeAnElement><SoShouldI>2025-01-02T03:04:05.000000006Z</SoShouldI><ThisIsTheManualOne>This is manual</ThisIsTheManualOne></InferUpperCamelName>", xmlString);

    }
}

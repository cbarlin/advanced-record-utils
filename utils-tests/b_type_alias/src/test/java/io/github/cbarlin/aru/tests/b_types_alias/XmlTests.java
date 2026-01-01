package io.github.cbarlin.aru.tests.b_types_alias;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.cbarlin.aru.tests.xml_util.ConvertToXml;

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
            
        final String xmlString = assertDoesNotThrow(() -> ConvertToXml.convertToXml(out -> assertDoesNotThrow(() -> someRecord.writeSelfTo(out))));
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><SomeTest AuthorName=\"This is an author\" one=\"42\" B=\"69\" C=\"13\"><BookName>And this is a book</BookName><vCsL>5</vCsL><vCsInt>69</vCsInt><vCsStr>Can this work?</vCsStr></SomeTest>", xmlString);

    }
}

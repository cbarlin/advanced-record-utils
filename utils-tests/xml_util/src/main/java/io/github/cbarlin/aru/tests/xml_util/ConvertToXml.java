package io.github.cbarlin.aru.tests.xml_util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;

public final class ConvertToXml {

    private ConvertToXml() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String convertToXml(final Consumer<XMLStreamWriter> consumer) throws IOException, XMLStreamException {
        final StringBuilder xmlStringBuilder = new StringBuilder();
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
            consumer.accept(streamWriter);
            streamWriter.writeEndDocument();
            // Flush and close the XML streams because apparently xml doesn't have autocloseable...
            streamWriter.flush();
            streamWriter.close();
            ws.flush();
            stringBuilderWriter.flush();
            return xmlStringBuilder.toString();
        }
    }
}

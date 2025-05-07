package io.github.cbarlin.aru.tests.b_infer_xml;

import java.time.OffsetDateTime;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

@AdvancedRecordUtils(xmlable = true, xmlOptions = @XmlOptions(inferXmlElementName = NameGeneration.MATCH))
public record InferMatchingName(
    String iShouldBeAnElement,
    OffsetDateTime soShouldI,
    @XmlTransient
    String iShouldBeIgnored,
    @XmlAttribute
    int randomAttribute,
    @XmlElement(name = "ThisIsTheManualOne")
    String someDefinedObject
) implements InferMatchingNameUtils.XML {

}

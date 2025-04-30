package io.github.cbarlin.aru.tests.d_eclipse_collections;

import org.eclipse.collections.api.list.ImmutableList;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@AdvancedRecordUtils(xmlable = true)
public record CanXmlTheRecord (
    @XmlAttribute(name = "SomeString")
    String someString,
    @XmlElement(name = "SelfReflection")
    @XmlElementWrapper(name = "WrapMe")
    ImmutableList<CanXmlTheRecord> anotherObject,
    @XmlElement(name = "SomeElementStrings")
    ImmutableList<String> someItemsAsElements
) implements CanXmlTheRecordUtils.All {

}

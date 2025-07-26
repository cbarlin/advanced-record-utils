package io.github.cbarlin.aru.tests.d_eclipse_collections;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import org.eclipse.collections.api.list.ImmutableList;

@AdvancedRecordUtils(xmlable = true)
public record CanXmlTheRecord (
    @XmlAttribute(name = "SomeString")
    String someString,
    @XmlElement(name = "SelfReflection")
    @XmlElementWrapper(name = "WrapMe")
    ImmutableList<CanXmlTheRecord> anotherObject,
    @XmlElementWrapper(name = "SomeElementStrings")
    @XmlElement(name = "SomeElementString")
    ImmutableList<String> someItemsAsElements,
    @XmlElementWrapper(name = "woot")
    @XmlElements({
        @XmlElement(name = "SomeA", type = SomeImplA.class),
        @XmlElement(name = "SomeB", type = SomeImplB.class),
    })
    ImmutableList<SomeIface> ifaceList
) implements CanXmlTheRecordUtils.All {

}

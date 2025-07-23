package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;

public record SecondLevelA(
    @XmlElements({
        @XmlElement(name = "ThirdLevelAFromA", type = ThirdLevelAFromA.class),
        @XmlElement(name = "someValueB", type = ThirdLevelBFromA.class),
        @XmlElement(name = "someValueC", type = ThirdLevelCFromA.class)
    })
    ThirdLevelInterface someValue
) {

}

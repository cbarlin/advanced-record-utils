package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import jakarta.xml.bind.annotation.XmlAttribute;

public record ThirdLevelBFromA(
    @XmlAttribute
    int someValue
) implements ThirdLevelInterface {

}

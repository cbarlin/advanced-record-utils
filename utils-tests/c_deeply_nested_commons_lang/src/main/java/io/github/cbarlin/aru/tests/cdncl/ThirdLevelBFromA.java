package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlAttribute;

public record ThirdLevelBFromA(
    @XmlAttribute
    int someValue
) implements ThirdLevelInterface {

}

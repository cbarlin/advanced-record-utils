package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.Optional;

public record SecondLevelA(
    @XmlElements({
        @XmlElement(name = "ThirdLevelAFromA", type = ThirdLevelAFromA.class),
        @XmlElement(name = "someValueB", type = ThirdLevelBFromA.class),
        @XmlElement(name = "someValueC", type = ThirdLevelCFromA.class)
    })
    ThirdLevelInterface someValue,
    @XmlTransient
    Optional<ThirdLevelInterface> optIface
) {

}

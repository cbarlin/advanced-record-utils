package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlElement;

public record FifthLevelA(
    @XmlElement
    SixthLevelA nowToSix
) {

}

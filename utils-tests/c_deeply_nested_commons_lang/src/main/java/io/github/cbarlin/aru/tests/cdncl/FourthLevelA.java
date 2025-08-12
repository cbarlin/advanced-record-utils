package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlElement;

public record FourthLevelA(
    @XmlElement
    FifthLevelA letsGoFive,
    @XmlElement
    SeventhLevelA oohNotLinear
) {

}

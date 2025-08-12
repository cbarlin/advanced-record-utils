package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlElement;

public record SecondLevelB(
    @XmlElement
    ThirdLevelAFromB thirdLevelAFromB
) {

}

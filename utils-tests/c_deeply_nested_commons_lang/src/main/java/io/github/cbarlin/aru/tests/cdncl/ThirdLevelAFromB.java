package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlElement;

public record ThirdLevelAFromB(
    @XmlElement
    FourthLevelA fourthLevelA,
    @XmlElement
    RecurringReference againImHere
) {

}

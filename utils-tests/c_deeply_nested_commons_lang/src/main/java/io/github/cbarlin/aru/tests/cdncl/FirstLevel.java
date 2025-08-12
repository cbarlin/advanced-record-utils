package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlElement;

public record FirstLevel(
    @XmlElement
    RecurringReference recurringReference,
    @XmlElement
    SecondLevelA secondLevelA,
    @XmlElement
    SecondLevelB secondLevelB,
    @XmlElement
    SecondLevelC secondLevelC
) {

}

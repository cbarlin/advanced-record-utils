package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

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

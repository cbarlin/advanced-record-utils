package io.github.cbarlin.aru.tests.cdncl;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public record ThirdLevelCFromA(
    @XmlAttribute
    double moreItemsIGuess, 
    @XmlElement
    RecurringReference lolHiAgain
) implements ThirdLevelInterface {

}

package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public record ThirdLevelCFromA(
    @XmlAttribute
    double moreItemsIGuess, 
    @XmlElement
    RecurringReference lolHiAgain
) implements ThirdLevelInterface {

}

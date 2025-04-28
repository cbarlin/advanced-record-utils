package io.github.cbarlin.aru.tests.c_deeply_nested_structure;

import jakarta.xml.bind.annotation.XmlElement;

public record SecondLevelB(
    @XmlElement
    ThirdLevelAFromB thirdLevelAFromB
) {

}

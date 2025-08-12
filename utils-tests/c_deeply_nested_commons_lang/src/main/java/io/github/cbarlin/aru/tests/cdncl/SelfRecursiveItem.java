package io.github.cbarlin.aru.tests.cdncl;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public record SelfRecursiveItem(
    @XmlElement
    String notRecursiveA,
    @XmlAttribute
    String notRecursiveB,
    @XmlAttribute
    String notRecursiveC,
    @XmlElement
    List<SelfRecursiveItem> recursionFtw
) {}

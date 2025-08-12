package io.github.cbarlin.aru.tests.cdncl;

import java.util.Set;

import io.github.cbarlin.aru.annotations.TypeConverter;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public record RecurringReference(
    @XmlAttribute
    String itemA,
    @XmlAttribute
    String itemB,
    @XmlAttribute
    String itemC,
    @XmlElement
    Set<SelfRecursiveItem> recurisveItems
) {

    @TypeConverter
    public static RecurringReference viaConverter(final String itmA, final String itmC) {
        return new RecurringReference(itmA, null, itmC, Set.of());
    }
}

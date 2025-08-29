package io.github.cbarlin.aru.tests.d_eclipse_collections;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;

@AdvancedRecordUtils(
    xmlOptions = @XmlOptions(inferXmlElementName = NameGeneration.UPPER_FIRST_LETTER),
    xmlable = true
)
public record SomeImplB(String anotherField, int iAmNotNeeded) implements SomeIface {

    public SomeImplB(String anotherField) {
        this(anotherField, 42);
    }
}

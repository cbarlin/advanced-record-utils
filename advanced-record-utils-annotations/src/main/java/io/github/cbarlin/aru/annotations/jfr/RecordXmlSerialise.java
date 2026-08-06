package io.github.cbarlin.aru.annotations.jfr;

import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("io.github.cbarlin.aru.RecordXmlSerialise")
@Label("Record XML Serialise")
@Description("A record was serialised to XML")
public final class RecordXmlSerialise extends AruAbstractEvent {

    public RecordXmlSerialise(final String utilsClass, final String targetClass) {
        super(utilsClass, targetClass);
    }
}

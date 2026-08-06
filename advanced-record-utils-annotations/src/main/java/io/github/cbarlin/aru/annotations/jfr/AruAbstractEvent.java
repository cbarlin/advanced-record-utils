package io.github.cbarlin.aru.annotations.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;

@Category({"Advanced Record Utils"})
@StackTrace(false)
public sealed abstract class AruAbstractEvent extends Event
permits RecordBuild, RecordDiffCreation, RecordXmlSerialise {
    @Label("Utils Class FQN")
    public final String utilsClass;

    @Label("Target Record FQN")
    public final String targetClass;

    protected AruAbstractEvent(final String utilsClass, final String targetClass) {
        super();
        this.utilsClass = utilsClass;
        this.targetClass = targetClass;
        this.begin();
    }
}

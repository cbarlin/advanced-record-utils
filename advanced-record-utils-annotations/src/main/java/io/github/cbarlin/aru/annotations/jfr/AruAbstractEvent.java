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
    public final Class<?> utilsClass;

    @Label("Target Record FQN")
    public final Class<?> targetClass;

    @Label("Current Thread")
    public final Thread currentThread = Thread.currentThread();

    protected AruAbstractEvent(final Class<?> utilsClass, final Class<?> targetClass) {
        super();
        this.utilsClass = utilsClass;
        this.targetClass = targetClass;
        this.begin();
    }
}

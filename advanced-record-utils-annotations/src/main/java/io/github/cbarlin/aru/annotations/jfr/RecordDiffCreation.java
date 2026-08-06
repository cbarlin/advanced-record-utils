package io.github.cbarlin.aru.annotations.jfr;

import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("io.github.cbarlin.aru.RecordDiffCreation")
@Label("Record Diff Creation")
@Description("A diff was computed between two instances of a record")
public final class RecordDiffCreation extends AruAbstractEvent {

    public RecordDiffCreation(final String utilsClass, final String targetClass) {
        super(utilsClass, targetClass);
    }
}

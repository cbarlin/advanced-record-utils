package io.github.cbarlin.aru.annotations.jfr;

import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("io.github.cbarlin.aru.RecordBuild")
@Label("Record Build")
@Description("A Builder was created, populated, and then the final object was constructed")
public class RecordBuild extends AruAbstractEvent {

    public RecordBuild(final String utilsClass, final String targetClass) {
        super(utilsClass, targetClass);
    }
}

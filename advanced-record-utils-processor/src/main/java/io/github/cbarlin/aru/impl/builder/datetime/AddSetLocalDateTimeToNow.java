package io.github.cbarlin.aru.impl.builder.datetime;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.impl.Constants.Names;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class})
@RequiresProperty(value = "setTimeNowMethods", equalTo = "true")
public final class AddSetLocalDateTimeToNow extends SetToNow {
    public AddSetLocalDateTimeToNow(final AnalysedRecord analysedRecord, final BuilderClass builderClass) {
        super(analysedRecord, Names.LOCAL_DATE_TIME, builderClass);
    }
}

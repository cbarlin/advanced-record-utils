package io.github.cbarlin.aru.impl.builder.datetime;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.impl.Constants.Names;

@ServiceProvider
public final class AddSetLocalDateTimeToNow extends SetToNow {
    public AddSetLocalDateTimeToNow() {
        super(Names.LOCAL_DATE_TIME);
    }
}

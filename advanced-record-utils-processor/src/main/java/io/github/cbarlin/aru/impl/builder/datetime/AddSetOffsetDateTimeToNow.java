package io.github.cbarlin.aru.impl.builder.datetime;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.impl.Constants.Names;

@ServiceProvider
public final class AddSetOffsetDateTimeToNow extends SetToNow {
    public AddSetOffsetDateTimeToNow() {
        super(Names.OFFSET_DATE_TIME);
    }
}

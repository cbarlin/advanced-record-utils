package io.github.cbarlin.aru.impl.builder.datetime;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.impl.Constants.Names;

@ServiceProvider
public final class AddSetZonedDateTimeToNow extends SetToNow {
    public AddSetZonedDateTimeToNow() {
        super(Names.ZONED_DATE_TIME);
    }
}

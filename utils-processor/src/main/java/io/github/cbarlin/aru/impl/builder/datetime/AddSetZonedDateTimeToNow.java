package io.github.cbarlin.aru.impl.builder.datetime;

import io.github.cbarlin.aru.impl.Constants.Names;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class AddSetZonedDateTimeToNow extends SetToNow {
    public AddSetZonedDateTimeToNow() {
        super(Names.ZONED_DATE_TIME);
    }
}

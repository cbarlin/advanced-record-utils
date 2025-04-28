package io.github.cbarlin.aru.impl.builder.datetime;

import io.github.cbarlin.aru.impl.Constants.Names;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class AddSetOffsetDateTimeToNow extends SetToNow {
    public AddSetOffsetDateTimeToNow() {
        super(Names.OFFSET_DATE_TIME);
    }
}

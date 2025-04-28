package io.github.cbarlin.aru.impl.builder.datetime;

import io.github.cbarlin.aru.impl.Constants.Names;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class AddSetLocalDateTimeToNow extends SetToNow {
    public AddSetLocalDateTimeToNow() {
        super(Names.LOCAL_DATE_TIME);
    }
}

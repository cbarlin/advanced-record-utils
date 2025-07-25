package io.github.cbarlin.aru.impl.types.collection.list;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARRAY_LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public final class JustListCollectionHandler extends ListCollectionHandler {

    public JustListCollectionHandler() {
        super(LIST, ARRAY_LIST);
    }
}

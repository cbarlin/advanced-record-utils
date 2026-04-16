package io.github.cbarlin.aru.impl.builder.map;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.maps.MapHandlerHelper;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, MapHandlerHelper.class})
public final class MapField extends RecordVisitor {

    private final BuilderClass builderClass;
    private final MapHandlerHelper mapHandlerHelper;

    public MapField(
        final AnalysedRecord analysedRecord,
        final BuilderClass builderClass,
        final MapHandlerHelper mapHandlerHelper
    ) {
        super(CommonsConstants.Claims.CORE_BUILDER_FIELD, analysedRecord);
        this.builderClass = builderClass;
        this.mapHandlerHelper = mapHandlerHelper;
    }

    @Override
    public int specificity() {
        return 6;
    }

    @Override
    protected boolean visitComponentImpl(
        final AnalysedComponent ignored
    ) {
        mapHandlerHelper.addField(builderClass.delegate());
        return true;
    }
}

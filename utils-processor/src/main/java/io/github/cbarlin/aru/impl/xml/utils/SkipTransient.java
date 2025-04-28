package io.github.cbarlin.aru.impl.xml.utils;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class SkipTransient extends XmlVisitor {

    public SkipTransient() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        return XmlTransientPrism.isPresent(analysedComponent.element().getAccessor());
    }
}

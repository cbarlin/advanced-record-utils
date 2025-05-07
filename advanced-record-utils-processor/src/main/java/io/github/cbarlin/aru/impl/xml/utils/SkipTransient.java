package io.github.cbarlin.aru.impl.xml.utils;

import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_TRANSIENT;

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
        // Some really high number so nothing else claims transient items
        return 25565;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        return analysedComponent.isPrismPresent(XML_TRANSIENT, XmlTransientPrism.class);
    }
}

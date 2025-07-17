package io.github.cbarlin.aru.impl.xml.utils;

import static io.github.cbarlin.aru.impl.Constants.Names.XML_TRANSIENT;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;

@ServiceProvider
public final class SkipTransient extends XmlVisitor {

    // This visitor should go first of the Xml Visitors as we want to skip over everything
    //   if something is transient. However, we can't use e.g. Integer.MAX_VALUE because
    //   this gets added to and we don't want an integer overflow
    private static final int HIGH_SPECIFICITY = 25565;

    public SkipTransient() {
        super(Claims.XML_WRITE_FIELD);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return HIGH_SPECIFICITY;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        return analysedComponent.isPrismPresent(XML_TRANSIENT, XmlTransientPrism.class);
    }
}

package io.github.cbarlin.aru.impl.xml.utils;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.wiring.XmlPerComponentScope;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;
import io.github.cbarlin.aru.prism.prison.XmlTransientPrism;
import jakarta.inject.Singleton;

@Singleton
@XmlPerComponentScope
@RequiresBean({XmlTransientPrism.class})
public final class SkipTransient extends XmlVisitor {

    // This visitor should go first of the Xml Visitors as we want to skip over everything
    //   if something is transient. However, we can't use e.g. Integer.MAX_VALUE because
    //   this gets added to and we don't want an integer overflow
    private static final int HIGH_SPECIFICITY = 25565;

    public SkipTransient(final XmlRecordHolder xmlRecordHolder) {
        super(Claims.XML_WRITE_FIELD, xmlRecordHolder);
    }

    @Override
    protected int innerSpecificity() {
        return HIGH_SPECIFICITY;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        return true;
    }
}

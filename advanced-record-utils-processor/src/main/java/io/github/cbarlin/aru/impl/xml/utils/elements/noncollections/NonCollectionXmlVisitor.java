package io.github.cbarlin.aru.impl.xml.utils.elements.noncollections;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.impl.xml.XmlRecordHolder;
import io.github.cbarlin.aru.impl.xml.XmlVisitor;

import java.util.Optional;

public abstract class NonCollectionXmlVisitor extends XmlVisitor {

    private final Optional<AnalysedComponent> analysedOptionalComponent;

    protected NonCollectionXmlVisitor(final ClaimableOperation claimableOperation, final XmlRecordHolder xmlHolder, final Optional<AnalysedOptionalComponent> analysedOptionalComponent) {
        super(claimableOperation, xmlHolder);
        this.analysedOptionalComponent = analysedOptionalComponent.map(AnalysedComponent.class::cast);
    }

    abstract protected boolean writeElementMethod(AnalysedComponent analysedComponent);

    @Override
    protected final boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        return this.writeElementMethod(analysedOptionalComponent.orElse(analysedComponent));
    }
}

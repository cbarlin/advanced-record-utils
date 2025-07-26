package io.github.cbarlin.aru.core.visitors;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.impl.types.OptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;

/**
 * An extension to a {@link RecordVisitor} that visits optional components
 */
public abstract class OptionalRecordVisitor extends RecordVisitor {

    protected final OptionalComponent optionalComponent;

    protected OptionalRecordVisitor(final ClaimableOperation claimableOperation, final AnalysedRecord analysedRecord, final OptionalComponent optionalComponent) {
        super(claimableOperation, analysedRecord);
        this.optionalComponent = optionalComponent;
    }

    @Override
    public final int specificity() {
        return 1 + optionalSpecificity();
    }

    /**
     * Once we have filtered this to a optional, how specific are we?
     */
    protected abstract int optionalSpecificity();

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        return analysedComponent instanceof OptionalComponent && visitOptionalComponent();
    }

    protected abstract boolean visitOptionalComponent();

}

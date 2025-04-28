package io.github.cbarlin.aru.core.visitors;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.impl.types.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;

/**
 * An extension to a {@link RecordVisitor} that visits optional components
 */
public abstract class OptionalRecordVisitor extends RecordVisitor {

    protected OptionalRecordVisitor(ClaimableOperation claimableOperation) {
        super(claimableOperation);
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
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        return analysedComponent instanceof AnalysedOptionalComponent apc && visitOptionalComponent(apc);
    }

    protected abstract boolean visitOptionalComponent(AnalysedOptionalComponent analysedOptionalComponent);

}

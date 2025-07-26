package io.github.cbarlin.aru.core.visitors;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedInterface;

public abstract class InterfaceVisitor extends AruVisitor<AnalysedInterface> {

    protected InterfaceVisitor(final ClaimableOperation claimableOperation, final AnalysedInterface analysedInterface) {
        super(claimableOperation, analysedInterface);
    }

    public final void visitInterface(final AnalysedInterface analysedInterface) {
        if (analysedInterface.attemptToClaim(this)) {
            final boolean claiming = visitInterfaceImpl(analysedInterface);
            if (!claiming) {
                analysedInterface.retractClaim(this);
            }
        }
    }

    protected abstract boolean visitInterfaceImpl(final AnalysedInterface analysedInterface);
}

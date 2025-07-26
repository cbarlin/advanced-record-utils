package io.github.cbarlin.aru.core.visitors;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedInterface;

public abstract class InterfaceVisitor extends AruVisitor<AnalysedInterface> {

    protected final AnalysedInterface analysedInterface;

    protected InterfaceVisitor(final ClaimableOperation claimableOperation, final AnalysedInterface analysedInterface) {
        super(claimableOperation, analysedInterface);
        this.analysedInterface = analysedInterface;
    }

    public final void visitInterface() {
        if (analysedInterface.attemptToClaim(this)) {
            final boolean claiming = visitInterfaceImpl();
            if (!claiming) {
                analysedInterface.retractClaim(this);
            }
        }
    }

    protected abstract boolean visitInterfaceImpl();
}

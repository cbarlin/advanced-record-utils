package io.github.cbarlin.aru.core.visitors;

import io.avaje.spi.Service;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedInterface;

@Service
public abstract class InterfaceVisitor extends AruVisitor<AnalysedInterface> {

    protected InterfaceVisitor(final ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    public final void visitInterface(final AnalysedInterface analysedInterface) {
        if (analysedInterface.attemptToClaim(this)) {
            configureLogging(analysedInterface);
            final boolean claiming = visitInterfaceImpl(analysedInterface);
            if (!claiming) {
                analysedInterface.retractClaim(this);
            }
        }
    }

    protected abstract boolean visitInterfaceImpl(final AnalysedInterface analysedInterface);
}

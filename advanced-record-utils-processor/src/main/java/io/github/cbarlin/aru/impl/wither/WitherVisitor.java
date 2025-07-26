package io.github.cbarlin.aru.impl.wither;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.github.cbarlin.aru.prism.prison.WitherOptionsPrism;

public abstract class WitherVisitor extends RecordVisitor {

    protected final WitherInterface witherInterface;
    protected final WitherOptionsPrism witherOptionsPrism;
    protected final BuilderOptionsPrism builderOptionsPrism;

    protected WitherVisitor(
        final ClaimableOperation claimableOperation,
        final WitherInterface witherInterface,
        final AnalysedRecord analysedRecord
    ) {
        super(claimableOperation, analysedRecord);
        this.builderOptionsPrism = utilsPrism.builderOptions();
        this.witherOptionsPrism = utilsPrism.witherOptions();
        this.witherInterface = witherInterface;
    }

    /**
     * The specificity within the wither
     */
    protected abstract int witherSpecificity();

    @Override
    public final int specificity() {
        return 1 + witherSpecificity();
    }
}

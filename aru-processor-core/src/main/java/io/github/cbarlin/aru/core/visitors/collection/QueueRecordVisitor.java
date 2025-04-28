package io.github.cbarlin.aru.core.visitors.collection;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

/**
 * An extension to a {@link RecordVisitor} that only visits list components
 */
public abstract class QueueRecordVisitor extends CollectionRecordVisitor {

    protected QueueRecordVisitor(ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    @Override
    protected final int collectionSpecificity() {
        return 1 + queueSpecificity();
    }
    
    /**
     * Once we have filtered down to a queue, how specific are we?
     */
    protected abstract int queueSpecificity();

    @Override
    protected final boolean visitCollectionComponent(AnalysedCollectionComponent acc) {
        return acc.isQueue() && visitQueueComponent(acc);
    }

    protected abstract boolean visitQueueComponent(AnalysedCollectionComponent analysedCollectionComponent);

}

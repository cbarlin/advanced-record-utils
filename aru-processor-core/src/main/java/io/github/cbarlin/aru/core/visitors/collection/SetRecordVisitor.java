package io.github.cbarlin.aru.core.visitors.collection;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

/**
 * An extension to a {@link RecordVisitor} that only visits list components
 */
public abstract class SetRecordVisitor extends CollectionRecordVisitor {

    protected SetRecordVisitor(ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    @Override
    protected final int collectionSpecificity() {
        return 1 + setSpecificity();
    }
    
    /**
     * Once we have filtered down to a set, how specific are we?
     */
    protected abstract int setSpecificity();

    @Override
    protected final boolean visitCollectionComponent(AnalysedCollectionComponent acc) {
        return acc.isSet() && visitSetComponent(acc);
    }

    protected abstract boolean visitSetComponent(AnalysedCollectionComponent analysedCollectionComponent);

}

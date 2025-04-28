package io.github.cbarlin.aru.core.visitors.collection;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

/**
 * An extension to a {@link RecordVisitor} that only visits list components
 */
public abstract class ListRecordVisitor extends CollectionRecordVisitor {

    protected ListRecordVisitor(ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    @Override
    protected final int collectionSpecificity() {
        return 1 + listSpecificity();
    }

    /**
     * Once we have filtered down to a list, how specific are we?
     */
    protected abstract int listSpecificity();

    @Override
    protected final boolean visitCollectionComponent(AnalysedCollectionComponent acc) {
        return acc.isList() && visitListComponent(acc);
    }

    protected abstract boolean visitListComponent(AnalysedCollectionComponent analysedCollectionComponent);

}

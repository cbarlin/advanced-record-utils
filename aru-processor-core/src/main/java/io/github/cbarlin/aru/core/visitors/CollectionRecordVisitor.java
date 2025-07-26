package io.github.cbarlin.aru.core.visitors;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * An extension to a {@link RecordVisitor} that only visits collection components
 */
public abstract class CollectionRecordVisitor extends RecordVisitor {

    protected final AnalysedCollectionComponent analysedCollectionComponent;

    protected CollectionRecordVisitor(final ClaimableOperation claimableOperation, final AnalysedRecord analysedRecord, final AnalysedCollectionComponent analysedCollectionComponent) {
        super(claimableOperation, analysedRecord);
        this.analysedCollectionComponent = analysedCollectionComponent;
    }

    @Override
    public final int specificity() {
        return 1 + collectionSpecificity();
    }

    /**
     * Once we have filtered this to a collection, how specific are we?
     */
    protected abstract int collectionSpecificity();

    @Override
    protected final boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        return visitCollectionComponent();
    }

    protected abstract boolean visitCollectionComponent();

    protected static String addNameMethodName(final AnalysedCollectionComponent acc) {
        return acc.settings().prism().builderOptions().adderMethodPrefix() + 
            capitalise(acc.name()) + 
            acc.settings().prism().builderOptions().adderMethodSuffix();
    }

    protected static String addCnToNameMethodName(final AnalysedCollectionComponent acc, final ClassName targetClassName) {
        return acc.settings().prism().builderOptions().adderMethodPrefix() + 
            targetClassName.simpleName() + 
            acc.settings().prism().builderOptions().multiTypeAdderBridge() + 
            capitalise(acc.name()) + 
            acc.settings().prism().builderOptions().adderMethodSuffix();
    }
}

package io.github.cbarlin.aru.core.visitors;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * An extension to a {@link RecordVisitor} that only visits collection components
 */
public abstract class CollectionRecordVisitor extends RecordVisitor {

    protected final AnalysedCollectionComponent analysedCollectionComponent;

    protected CollectionRecordVisitor(
        final ClaimableOperation claimableOperation, 
        final AnalysedRecord analysedRecord, 
        final AnalysedCollectionComponent analysedCollectionComponent
    ) {
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

    protected String addNameMethodName() {
        return analysedCollectionComponent.settings().prism().builderOptions().adderMethodPrefix() + 
            capitalise(analysedCollectionComponent.name()) + 
            analysedCollectionComponent.settings().prism().builderOptions().adderMethodSuffix();
    }

    protected String addCnToNameMethodName(final ClassName targetClassName) {
        return analysedCollectionComponent.settings().prism().builderOptions().adderMethodPrefix() + 
            targetClassName.simpleName() + 
            analysedCollectionComponent.settings().prism().builderOptions().multiTypeAdderBridge() + 
            capitalise(analysedCollectionComponent.name()) + 
            analysedCollectionComponent.settings().prism().builderOptions().adderMethodSuffix();
    }
}

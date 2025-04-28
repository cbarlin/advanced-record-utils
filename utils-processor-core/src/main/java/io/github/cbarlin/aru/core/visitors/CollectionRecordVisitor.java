package io.github.cbarlin.aru.core.visitors;

import java.util.Locale;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;

import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * An extension to a {@link RecordVisitor} that only visits collection components
 */
public abstract class CollectionRecordVisitor extends RecordVisitor {

    protected CollectionRecordVisitor(ClaimableOperation claimableOperation) {
        super(claimableOperation);
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
        return analysedComponent instanceof AnalysedCollectionComponent acc && visitCollectionComponent(acc);
    }

    protected abstract boolean visitCollectionComponent(AnalysedCollectionComponent analysedCollectionComponent);

    public static String addNameMethodName(final AnalysedCollectionComponent acc) {
        return acc.settings().prism().builderOptions().adderMethodPrefix() + 
            capitalise(acc.name()) + 
            acc.settings().prism().builderOptions().adderMethodSuffix();
    }

    public static String addCnToNameMethodName(final AnalysedCollectionComponent acc, final ClassName targetClassName) {
        return acc.settings().prism().builderOptions().adderMethodPrefix() + 
            targetClassName.simpleName() + 
            acc.settings().prism().builderOptions().multiTypeAdderBridge() + 
            capitalise(acc.name()) + 
            acc.settings().prism().builderOptions().adderMethodSuffix();
    }

    private static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }
}

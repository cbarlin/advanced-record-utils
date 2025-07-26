package io.github.cbarlin.aru.impl.merger;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.misc.MatchingInterface;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.github.cbarlin.aru.prism.prison.MergerOptionsPrism;
import io.micronaut.sourcegen.javapoet.TypeName;

public abstract class MergerVisitor extends RecordVisitor {

    private static final String MERGE_STATIC_METHOD_FMT = "merge%s";

    protected final ToBeBuilt mergerInterface;
    protected final ToBeBuilt mergerStaticClass;
    protected final BuilderOptionsPrism builderOptionsPrism;
    protected final MergerOptionsPrism mergerOptionsPrism;
    protected final MatchingInterface matchingInterface;

    protected MergerVisitor(
        final ClaimableOperation claimableOperation,
        final MergerHolder mergerHolder
    ) {
        super(claimableOperation, mergerHolder.analysedRecord());
        this.mergerInterface = mergerHolder.mergerInterface();
        this.mergerStaticClass = mergerHolder.mergerStaticClass();
        this.builderOptionsPrism = mergerHolder.builderOptionsPrism();
        this.mergerOptionsPrism = mergerHolder.mergerOptionsPrism();
        this.matchingInterface = mergerHolder.matchingInterface();
    }

    protected abstract int innerSpecificity();

    @Override
    public final int specificity() {
        return 1 + innerSpecificity();
    }

    protected static String mergeStaticMethodName(final TypeName originalTypeName) {
        return MERGE_STATIC_METHOD_FMT.formatted(typeNameToPartialMethodName(originalTypeName));
    }
}

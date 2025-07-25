package io.github.cbarlin.aru.impl.merger;

import static io.github.cbarlin.aru.impl.Constants.Claims.INTERNAL_MATCHING_IFACE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.MERGER_UTILS_CLASS;

import java.util.Locale;

import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.BuilderOptionsPrism;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.MergerOptionsPrism;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.TypeName;

public abstract class MergerVisitor extends RecordVisitor {

    private static final String MERGE_STATIC_METHOD_FMT = "merge%s";

    protected ToBeBuilt mergerInterface;
    protected ToBeBuilt mergerStaticClass;
    protected BuilderOptionsPrism builderOptionsPrism;
    protected MergerOptionsPrism mergerOptionsPrism;
    protected ToBeBuilt matchingInterface;

    protected MergerVisitor(final ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    protected abstract boolean innerIsApplicable(final AnalysedRecord analysedRecord);

    protected abstract int innerSpecificity();

    @Override
    public final boolean isApplicable(final AnalysedRecord analysedRecord) {
        if (Boolean.TRUE.equals(analysedRecord.settings().prism().merger())) {
            matchingInterface = analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, INTERNAL_MATCHING_IFACE);
            mergerOptionsPrism = analysedRecord.settings().prism().mergerOptions();
            builderOptionsPrism = analysedRecord.settings().prism().builderOptions();
            final String generatedName = mergerOptionsPrism.mergerName();
            mergerInterface = analysedRecord.utilsClassChildInterface(generatedName, Claims.MERGER_IFACE);
            mergerStaticClass = analysedRecord.utilsClassChildClass(MERGER_UTILS_CLASS, Claims.MERGER_STATIC_CLASS);
            return innerIsApplicable(analysedRecord);
        }
        return false;
    }

    @Override
    public final int specificity() {
        return 1 + innerSpecificity();
    }

    protected static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }

    protected static String mergeStaticMethodName(final TypeName originalTypeName) {
        return MERGE_STATIC_METHOD_FMT.formatted(typeNameToPartialMethodName(originalTypeName));
    }
}

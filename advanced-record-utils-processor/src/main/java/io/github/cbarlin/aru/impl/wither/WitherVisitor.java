package io.github.cbarlin.aru.impl.wither;

import static io.github.cbarlin.aru.impl.Constants.Claims.WITHER_IFACE;

import java.util.Locale;

import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.BuilderOptionsPrism;
import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism.WitherOptionsPrism;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

public abstract class WitherVisitor extends RecordVisitor {

    protected ToBeBuilt witherInterface;
    protected WitherOptionsPrism witherOptionsPrism;
    protected BuilderOptionsPrism builderOptionsPrism;

    protected WitherVisitor(final ClaimableOperation claimableOperation) {
        super(claimableOperation);
    }

    /**
     * The specificity within the wither
     */
    protected abstract int witherSpecificity();

    /**
     * Beyond the wither being enabled, is this item applicable?
     */
    protected abstract boolean isWitherApplicable(final AnalysedRecord analysedRecord);

    @Override
    public final int specificity() {
        return 1 + witherSpecificity();
    }

    @Override
    public final boolean isApplicable(final AnalysedRecord analysedRecord) {
        final boolean proceed = analysedRecord.settings().prism().wither() && isWitherApplicable(analysedRecord);
        if (proceed) {
            this.builderOptionsPrism = analysedRecord.settings().prism().builderOptions();
            this.witherOptionsPrism = analysedRecord.settings().prism().witherOptions();
            final String generatedName = witherOptionsPrism.witherName();
            this.witherInterface = analysedRecord.utilsClassChildInterface(generatedName, WITHER_IFACE);
        }
        return proceed;
    }

    public static String capitalise(final String variableName) {
        return (variableName.length() < 2) ? variableName.toUpperCase(Locale.ROOT)
                : (Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1));
    }
}

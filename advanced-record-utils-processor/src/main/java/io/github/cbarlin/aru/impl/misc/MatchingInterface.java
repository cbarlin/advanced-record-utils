package io.github.cbarlin.aru.impl.misc;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.IToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;

public record MatchingInterface(
    ToBeBuilt delegate,
    AnalysedRecord analysedRecord
) implements IToBeBuilt<ToBeBuilt> {

    public ClaimableOperation claimableOperation() {
        return Claims.INTERNAL_MATCHING_IFACE;
    }
}

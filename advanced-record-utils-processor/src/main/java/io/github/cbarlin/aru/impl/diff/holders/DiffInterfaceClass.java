package io.github.cbarlin.aru.impl.diff.holders;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.IToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.impl.Constants.Claims;

public record DiffInterfaceClass(
    ToBeBuilt delegate
) implements IToBeBuilt<ToBeBuilt> {
    public static ClaimableOperation claimableOperation() {
        return Claims.DIFFER_IFACE;
    }
}

package io.github.cbarlin.aru.impl.diff.holders;

import java.util.HashSet;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.IToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.impl.Constants.Claims;

public record DiffStaticClass(
    ToBeBuilt delegate,
    HashSet<String> createdMethods
) implements IToBeBuilt<ToBeBuilt> {
    public static ClaimableOperation claimableOperation() {
        return Claims.DIFFER_UTILS;
    }
}

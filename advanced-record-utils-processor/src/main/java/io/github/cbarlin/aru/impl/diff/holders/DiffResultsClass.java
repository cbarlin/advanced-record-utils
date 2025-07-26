package io.github.cbarlin.aru.impl.diff.holders;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.IToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.MethodSpec;

public record DiffResultsClass (
    ToBeBuilt delegate,
    MethodSpec.Builder recordConstructor,
    MethodSpec.Builder interfaceConstructor
) implements IToBeBuilt<ToBeBuilt> {
    public static ClaimableOperation claimableOperation() {
        return Claims.DIFFER_RESULT;
    }
}

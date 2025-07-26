package io.github.cbarlin.aru.impl.wither;

import io.github.cbarlin.aru.core.artifacts.IToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;

public record WitherInterface(
    ToBeBuilt delegate
) implements IToBeBuilt<ToBeBuilt> {

}

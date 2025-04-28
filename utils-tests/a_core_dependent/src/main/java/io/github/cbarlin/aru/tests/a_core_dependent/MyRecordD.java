package io.github.cbarlin.aru.tests.a_core_dependent;

import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordB;

public record MyRecordD(
    MyRecordB theInterface
) {}

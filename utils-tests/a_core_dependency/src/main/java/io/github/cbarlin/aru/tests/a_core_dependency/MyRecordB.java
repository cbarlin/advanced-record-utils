package io.github.cbarlin.aru.tests.a_core_dependency;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;

@AdvancedRecordUtils(logGeneration = LoggingGeneration.SLF4J_UTILS_CLASS, createAllInterface = true, wither = true, merger = true)
public record MyRecordB(
    MyRecordA otherItem,
    MyRecordA woo,
    MyRecordB recursionFtw,
    AnEnumInDep andImAnEnum
) {

}

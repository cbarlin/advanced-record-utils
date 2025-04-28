package io.github.cbarlin.aru.tests.a_core_dependency;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;

@AdvancedRecordUtils(useInterface = SomeInterface.class)
public record MyRecordC(
    MyRecordB bItem
) implements SomeInterface {}

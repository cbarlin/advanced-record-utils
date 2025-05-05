package io.github.cbarlin.aru.tests.b_types_alias;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;

@AdvancedRecordUtils(builderOptions = @BuilderOptions(nullReplacesNotNull = false))
public record OtherRecord(
    AuthorName authorName,
    RandomIntA randomIntA
) {}

package io.github.cbarlin.aru.tests.a_core_dependent;

import java.util.Optional;

public record MyRecordE(
    MyRecordD myRecordD,
    Optional<String> someOtherValue
) {

}

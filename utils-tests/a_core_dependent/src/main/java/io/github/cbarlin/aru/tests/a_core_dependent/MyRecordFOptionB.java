package io.github.cbarlin.aru.tests.a_core_dependent;

public record MyRecordFOptionB(
    MyRecordE myRecordE,
    MyRecordD anotherRecordD
) implements SomeSealedInterface {

}

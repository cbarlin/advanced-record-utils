package io.github.cbarlin.aru.tests.a_core_dependent;

public sealed interface SomeSealedInterface permits MyRecordFOptionA, MyRecordFOptionB {
    MyRecordE myRecordE();
}

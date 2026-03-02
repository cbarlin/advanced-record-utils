package io.github.cbarlin.aru.tests.a_core_dependency.hidden;

// If `b_type_alias` compiles, then it's successfully dropped the reference to this record
@AnotherMetaAnnotation
public record SomeHiddenRecord(
    String value
) {
}

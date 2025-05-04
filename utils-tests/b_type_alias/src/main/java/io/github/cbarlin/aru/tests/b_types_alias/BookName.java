package io.github.cbarlin.aru.tests.b_types_alias;

import io.github.cbarlin.aru.annotations.aliases.StringAlias;

public record BookName (String value) implements StringAlias {

}

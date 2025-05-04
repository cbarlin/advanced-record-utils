package io.github.cbarlin.aru.tests.b_types_alias;

import io.github.cbarlin.aru.annotations.aliases.StringAlias;

public record AuthorName(String value) implements StringAlias {

}

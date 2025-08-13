import org.jspecify.annotations.NullMarked;

/**
 * Dependency for users.
 * <p>
 * Basic usage is available at <a href="https://github.com/cbarlin/advanced-record-utils">the GitHub page</a>
 */
@NullMarked
module io.github.cbarlin.aru.annotations {

    exports io.github.cbarlin.aru.annotations;
    exports io.github.cbarlin.aru.annotations.aliases;

    requires java.base;
    requires transitive org.jspecify;
}

import org.jspecify.annotations.NullMarked;

/**
 * Dependency for users.
 * <p>
 * Basic useage is available at https://github.com/cbarlin/advanced-record-utils
 */
@NullMarked
module io.github.cbarlin.aru.annotations {

    exports io.github.cbarlin.aru.annotations;

    requires java.base;
    requires static org.jspecify;
}

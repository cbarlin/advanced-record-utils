import org.jspecify.annotations.NullMarked;

@NullMarked
module io.github.cbarlin.aru.annotations {

    exports io.github.cbarlin.aru.annotations;

    requires java.base;
    requires static org.jspecify;
}

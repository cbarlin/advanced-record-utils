module io.github.cbarlin.aru.tests.a_core_dependency {
    exports io.github.cbarlin.aru.tests.a_core_dependency;

    requires transitive io.github.cbarlin.aru.annotations;
    requires io.jstach.rainbowgum.slf4j;
    requires io.jstach.rainbowgum.pattern;
    requires io.jstach.rainbowgum;
    requires transitive org.slf4j;
    requires transitive org.jspecify;
}

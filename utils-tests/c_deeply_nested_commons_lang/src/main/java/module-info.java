module io.github.cbarlin.aru.tests.c_deeply_nested_structure {
    exports io.github.cbarlin.aru.tests.c_deeply_nested_structure;
    requires transitive io.github.cbarlin.aru.tests.a_core_dependency;
    requires java.xml;
    requires jakarta.xml.bind;
    requires io.avaje.jsonb;
    requires org.apache.commons.lang3;

    provides io.avaje.jsonb.spi.JsonbExtension 
        with io.github.cbarlin.aru.tests.c_deeply_nested_structure.jsonb.GeneratedJsonComponent;
}

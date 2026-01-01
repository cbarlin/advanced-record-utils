import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordBUtils;

@AdvancedRecordUtils.ImportLibraryUtils({MyRecordBUtils.class})
module b.type.alias {
    requires io.github.cbarlin.aru.annotations;
    requires io.github.cbarlin.aru.tests.a_core_dependency;
    requires jakarta.xml.bind;
    requires java.xml;
    requires org.jspecify;
    requires valueclasses;
    // This is apparently required by valueclasses
    requires org.apache.commons.lang3;
}
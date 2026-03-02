import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordBUtils;

// The `MyRecordBUtils` should reference a class in `a_core_dependency` that isn't
//   available to this module. If compilation succeeds, we've not included it as a
//   root record when generating the ones for this module.
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
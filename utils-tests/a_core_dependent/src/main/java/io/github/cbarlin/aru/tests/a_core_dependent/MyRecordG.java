package io.github.cbarlin.aru.tests.a_core_dependent;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuiltCollectionType;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordA;

import java.util.List;
import java.util.Set;

@AdvancedRecordUtils(
    builderOptions = @BuilderOptions(
        copyCreationName = "from",
        builtCollectionType = BuiltCollectionType.AUTO
    ),
    attemptToFindExistingUtils = true
)
public record MyRecordG(
    SomeSealedInterface theSealedOne,
    List<SomeSealedInterface> listOfSealed,
    MyRecordA anA,
    Set<MyRecordA> moreAItems
) {

}

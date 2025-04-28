package io.github.cbarlin.aru.tests.a_core_dependent;

import java.util.List;
import java.util.Set;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordA;

@AdvancedRecordUtils(
    builderOptions = @BuilderOptions(
        copyCreationName = "from"
    )
)
public record MyRecordG(
    SomeSealedInterface theSealedOne,
    List<SomeSealedInterface> listOfSealed,
    MyRecordA anA,
    Set<MyRecordA> moreAItems
) {

}

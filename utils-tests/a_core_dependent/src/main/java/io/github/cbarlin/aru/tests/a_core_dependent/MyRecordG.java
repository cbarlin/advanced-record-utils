package io.github.cbarlin.aru.tests.a_core_dependent;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuilderOptions;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuiltCollectionType;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordA;

import javax.lang.model.element.PackageElement;
import java.util.List;
import java.util.Objects;
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
    Set<MyRecordA> moreAItems,
    String appendMe
) {

    @AdvancedRecordUtils.BeforeBuild
    public static void callMeMaybe(final MyRecordGUtils.Builder builder) {
        if (Objects.isNull(builder.appendMe()) || builder.appendMe().isBlank()) {
            builder.appendMe("Nothing here!");
        } else {
            builder.appendMe(builder.appendMe() + " and me!");
        }
    }

    @AdvancedRecordUtils.BuilderExtension
    public static void sealedExt(final MyRecordGUtils.Builder b, final SomeSealedInterface someSealedInterface) {
        b.addListOfSealed(someSealedInterface)
            .theSealedOne(someSealedInterface);
    }

    @AdvancedRecordUtils.BuilderExtension(fromInterface = GeeBuilder.class)
    public static boolean aItemExt(final MyRecordGUtils.Builder b, final MyRecordA a) {
        if (!b.moreAItems().contains(a)) {
            b.addMoreAItems(a)
                .anA(a);
            return true;
        }
        return false;
    }
}

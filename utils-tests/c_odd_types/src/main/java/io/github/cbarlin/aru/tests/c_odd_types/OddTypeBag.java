package io.github.cbarlin.aru.tests.c_odd_types;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordA;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

// Order of components vs the constructor is intentional. We need to ensure that
//   the "targetConstructor" does not care about the order of the original components,
//   or where the omitted element is
@AdvancedRecordUtils(
    merger = true,
    wither = true,
    xmlable = true,
    xmlOptions = @XmlOptions(inferXmlElementName = NameGeneration.UPPER_FIRST_LETTER),
    createAllInterface = true,
    logGeneration = LoggingGeneration.SLF4J_GENERATED_UTIL_INTERFACE,
    attemptToFindExistingUtils = true
)
public record OddTypeBag(
    @XmlElementWrapper(name = "WrapperOfLSO")
    Optional<List<String>> listOfItems,
    OptionalInt someOptionalInt,
    OptionalLong someOptionalLong,
    OptionalDouble optionalDouble,
    Optional<Set<MyRecordA>> setOfMyRecA,
    String thisShouldNotBeInTheBuilder,
    Optional<String> someOptional,
    List<OptionalInt> moreOptionalInts,
    UUID id
) implements OddTypeBagUtils.All {

    private static final String DEFAULT_NOT_IN_BUILDER = "This is horse isn't from here";

    @AdvancedRecordUtils.TargetConstructor
    public OddTypeBag(
        Optional<List<String>> listOfItems,
        OptionalDouble optionalDouble,
        OptionalInt someOptionalInt,
        Optional<Set<MyRecordA>> setOfMyRecA,
        Optional<String> someOptional,
        OptionalLong someOptionalLong,
        List<OptionalInt> moreOptionalInts,
        UUID id
    ) {
        this(listOfItems, someOptionalInt, someOptionalLong, optionalDouble, setOfMyRecA, DEFAULT_NOT_IN_BUILDER, someOptional, moreOptionalInts, id);
    }

}

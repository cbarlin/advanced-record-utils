package io.github.cbarlin.aru.tests.c_odd_types;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.LoggingGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;
import io.github.cbarlin.aru.tests.a_core_dependency.MyRecordA;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@AdvancedRecordUtils(
    merger = true,
    wither = true,
    xmlable = true,
    xmlOptions = @XmlOptions(inferXmlElementName = NameGeneration.UPPER_FIRST_LETTER),
    createAllInterface = true,
    logGeneration = LoggingGeneration.SLF4J_GENERATED_UTIL_INTERFACE
)
public record OddTypeBag(
    @XmlElementWrapper(name = "WrapperOfLSO")
    Optional<List<String>> listOfItems,
    OptionalInt someOptionalInt,
    OptionalLong someOptionalLong,
    OptionalDouble optionalDouble,
    Optional<Set<MyRecordA>> setOfMyRecA,
    String thisShouldNotBeInTheBuilder
) implements OddTypeBagUtils.All {

    @AdvancedRecordUtils.TargetConstructor
    public OddTypeBag(
        Optional<List<String>> listOfItems,
        OptionalInt someOptionalInt,
        OptionalLong someOptionalLong,
        OptionalDouble optionalDouble,
        Optional<Set<MyRecordA>> setOfMyRecA
    ) {
        this(listOfItems, someOptionalInt, someOptionalLong, optionalDouble, setOfMyRecA, "This is horse isn't from here");
    }

}

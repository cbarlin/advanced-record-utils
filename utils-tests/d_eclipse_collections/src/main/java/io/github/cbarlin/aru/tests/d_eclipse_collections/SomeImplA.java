package io.github.cbarlin.aru.tests.d_eclipse_collections;

import io.avaje.validation.constraints.NotBlank;
import io.avaje.validation.constraints.Valid;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.NameGeneration;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.XmlOptions;
import org.jspecify.annotations.NonNull;

@AdvancedRecordUtils(
    xmlOptions = @XmlOptions(inferXmlElementName = NameGeneration.UPPER_FIRST_LETTER),
    xmlable = true,
    builderOptions = @AdvancedRecordUtils.BuilderOptions(
        validatedBuilder = AdvancedRecordUtils.ValidationApi.AVAJE
    )
)
@Valid
public record SomeImplA(
    @NotBlank @NonNull
    String field
) implements SomeIface {

}

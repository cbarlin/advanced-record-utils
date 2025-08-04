package io.github.cbarlin.aru.impl.externs;

import javax.annotation.processing.ProcessingEnvironment;

import io.avaje.inject.Component;
import io.avaje.inject.External;
import io.github.cbarlin.aru.core.PreviousCompilationChecker;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.UseInterfaceDefaultClass;
import io.github.cbarlin.aru.impl.wiring.GlobalScope;

@Component
@GlobalScope
public record ExternalFromGlobal(
    @External UtilsProcessingContext upc,
    @External PreviousCompilationChecker pcc,
    @External UseInterfaceDefaultClass uidc,
    @External ProcessingEnvironment penv
) {

}

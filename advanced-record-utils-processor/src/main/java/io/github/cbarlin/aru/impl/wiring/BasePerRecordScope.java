package io.github.cbarlin.aru.impl.wiring;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.processing.ProcessingEnvironment;

import io.avaje.inject.InjectModule;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import jakarta.inject.Scope;

@Scope
@InjectModule(
    name = "BasePerRecord",
    requires = {
        GlobalScope.class,
    }
)
@Inherited
@Retention(SOURCE)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
public @interface BasePerRecordScope {

}

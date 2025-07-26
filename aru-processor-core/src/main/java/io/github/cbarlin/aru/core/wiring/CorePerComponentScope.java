package io.github.cbarlin.aru.core.wiring;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.lang.model.element.RecordComponentElement;

import io.avaje.inject.InjectModule;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.AnalysedOptionalComponent;
import io.github.cbarlin.aru.core.types.components.BasicAnalysedComponent;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import jakarta.inject.Scope;

@Scope
@InjectModule(
    name = "CorePerComponent",
    requires = {
        CorePerRecordScope.class,
        RecordComponentElement.class,
    },
    provides = {
        AdvancedRecordUtilsPrism.class,
        AdvRecUtilsSettings.class,
        AnalysedCollectionComponent.class,
        AnalysedComponent.class,
        AnalysedOptionalComponent.class,
        AnalysedRecord.class,
        BasicAnalysedComponent.class,
        BuilderClass.class,
        BuilderOptionsPrism.class,
        CoreGlobalScope.class,
        CorePerRecordScope.class,
        RecordComponentElement.class,
        UtilsClass.class,
    }
)
@Inherited
@Retention(SOURCE)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
public @interface CorePerComponentScope {

}

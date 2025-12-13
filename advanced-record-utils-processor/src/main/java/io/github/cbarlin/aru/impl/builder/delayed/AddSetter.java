package io.github.cbarlin.aru.impl.builder.delayed;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.BuilderClass;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresProperty(value = "delayNestedBuild", equalTo = "true")
@RequiresBean({ConstructorComponent.class, ComponentTargetingRecord.class})
public final class AddSetter extends RecordVisitor {
    private final BuilderClass builderClass;
    private final ClassName builderClassName;
    private final ComponentTargetingRecord componentTargetingRecord;

    public AddSetter(final AnalysedRecord analysedRecord, final BuilderClass builderClass, final ComponentTargetingRecord componentTargetingRecord) {
        super(CommonsConstants.Claims.CORE_BUILDER_SETTER, analysedRecord);
        this.builderClass = builderClass;
        this.builderClassName = builderClass.className();
        this.componentTargetingRecord = componentTargetingRecord;
    }

    @Override
    public int specificity() {
        return 4;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String name = analysedComponent.name();
        final ParameterSpec param = ParameterSpec.builder(analysedComponent.typeNameNullable(), name, Modifier.FINAL)
                                                 .addJavadoc("The replacement value")
                                                 .build();

        final var method = builderClass.createMethod(analysedComponent.name(), claimableOperation, analysedComponent)
                                       .addJavadoc("Updates the value of {@code $L}", name)
                                       .returns(builderClassName)
                                       .addParameter(param)
                                       .addModifiers(Modifier.PUBLIC);

        if (!Boolean.FALSE.equals(settings.prism().builderOptions().nullReplacesNotNull())) {
            method.addStatement(
                    "this.$L = $T.nonNull($L) ? $T.$L($L) : null",
                      name,
                      OBJECTS,
                      name,
                      componentTargetingRecord.target().utilsClassName(),
                      componentTargetingRecord.target().settings().prism().builderOptions().copyCreationName(),
                      name
                  )
                  .addJavadoc("\n<p>\n")
                  .addJavadoc("Supplying a null value will set the current value to null");
        } else {
            method.addStatement(
                      "this.$L = $T.nonNull($L) ? $T.$L($L) : this.$L",
                      name,
                      OBJECTS,
                      name,
                      componentTargetingRecord.target().utilsClassName(),
                      componentTargetingRecord.target().settings().prism().builderOptions().copyCreationName(),
                      name,
                      name
                  )
                  .addJavadoc("\n<p>\n")
                  .addJavadoc("Supplying a null value won't replace a set value");
        }

        method.addStatement("return this");

        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
    }
}

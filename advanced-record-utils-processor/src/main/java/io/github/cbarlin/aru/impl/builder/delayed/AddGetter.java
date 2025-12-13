package io.github.cbarlin.aru.impl.builder.delayed;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresProperty(value = "delayNestedBuild", equalTo = "true")
@RequiresBean({ConstructorComponent.class, ComponentTargetingRecord.class})
public final class AddGetter extends RecordVisitor {
    private final ComponentTargetingRecord componentTargetingRecord;

    public AddGetter(final AnalysedRecord analysedRecord, final ComponentTargetingRecord componentTargetingRecord) {
        super(CommonsConstants.Claims.CORE_BUILDER_GETTER, analysedRecord);
        this.componentTargetingRecord = componentTargetingRecord;
    }

    @Override
    public int specificity() {
        return 4;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String name = analysedComponent.name();
        final MethodSpec.Builder method = analysedRecord.builderArtifact()
            .createMethod(name, claimableOperation, analysedComponent.element())
            .returns(analysedComponent.typeNameNullable())
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Returns the current value of {@code $L}\n", name)
            .addStatement(
                "return $T.nonNull(this.$L) ? this.$L.$L() : null",
                OBJECTS,
                name,
                name,
                componentTargetingRecord.target().prism().builderOptions().buildMethodName()
            );

        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
    }
}

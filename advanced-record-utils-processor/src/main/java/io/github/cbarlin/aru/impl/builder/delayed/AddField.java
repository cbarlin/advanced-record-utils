package io.github.cbarlin.aru.impl.builder.delayed;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.List;

@Component
@BuilderPerComponentScope
@RequiresProperty(value = "fluent", equalTo = "true")
@RequiresProperty(value = "delayNestedBuild", equalTo = "true")
@RequiresBean({ConstructorComponent.class, ComponentTargetingRecord.class})
public final class AddField extends RecordVisitor {
    private final ComponentTargetingRecord componentTargetingRecord;

    public AddField(final AnalysedRecord analysedRecord, final ComponentTargetingRecord componentTargetingRecord) {
        super(CommonsConstants.Claims.CORE_BUILDER_FIELD, analysedRecord);
        this.componentTargetingRecord = componentTargetingRecord;
    }

    @Override
    public int specificity() {
        return 4;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent ignored) {
        // Annotating the field results in a compile error... for some reason? I'm guessing the ambiguity
        //   of if the Utils class part is "null" or the builder part - which doesn't make sense in our context,
        //   but I can see it make sense in other contexts.
        final ClassName builderClassName = componentTargetingRecord.target().builderArtifact().className();
        final ClassName nullable = builderClassName.annotated(List.of(AnnotationSpec.builder(CommonsConstants.Names.NULLABLE).build()));
        final FieldSpec spec = FieldSpec.builder(
                                            nullable,
                                            componentTargetingRecord.name(),
                                            Modifier.PRIVATE
                                        )
                                        .build();
        analysedRecord.builderArtifact().addField(spec);
        return true;

    }
}

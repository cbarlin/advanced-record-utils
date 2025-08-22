package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, AnalysedCollectionComponent.class})
@RequiresProperty(value = "setToNullMethods", equalTo = "true")
@RequiresProperty(value = "nullReplacesNotNull", equalTo = "true")
@RequiresProperty(value = "buildNullCollectionToEmpty", equalTo = "true")
public final class SetToNullCollectionNeverNull extends RecordVisitor {

    public SetToNullCollectionNeverNull(final AnalysedRecord analysedRecord) {
        super(Constants.Claims.BUILDER_SET_TO_NULL, analysedRecord);
    }

    @Override
    public int specificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String methodName = "set" + analysedComponent.nameFirstLetterCaps() + "ToNull";
        final MethodSpec.Builder builder = analysedRecord.builderArtifact().createMethod(methodName, claimableOperation)
            .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
            .addStatement(
                "this.$L.clear()",
                analysedComponent.name()
            )
            .addStatement("return this")
            .addAnnotation(Constants.Names.NON_NULL)
            .addJavadoc(
                "Sets the value of $L to an empty collection.\nThis is because {@code null} collections become empty.\n",
                analysedComponent.name()
            )
            .returns(analysedRecord.builderArtifact().className());
        AnnotationSupplier.addGeneratedAnnotation(builder, this);

        return true;
    }
}

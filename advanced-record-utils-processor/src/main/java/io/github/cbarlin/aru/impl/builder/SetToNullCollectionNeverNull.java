package io.github.cbarlin.aru.impl.builder;

import io.avaje.inject.Component;
import io.avaje.inject.RequiresBean;
import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.components.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.components.ConstructorComponent;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants;
import io.github.cbarlin.aru.impl.types.AnalysedOptionalCollection;
import io.github.cbarlin.aru.impl.wiring.BuilderPerComponentScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;
import java.util.Optional;

@Component
@BuilderPerComponentScope
@RequiresBean({ConstructorComponent.class, AnalysedCollectionComponent.class})
@RequiresProperty(value = "setToNullMethods", equalTo = "true")
@RequiresProperty(value = "nullReplacesNotNull", equalTo = "true")
@RequiresProperty(value = "buildNullCollectionToEmpty", equalTo = "true")
public final class SetToNullCollectionNeverNull extends RecordVisitor {

    private final Optional<AnalysedOptionalCollection> optionalCollection;

    public SetToNullCollectionNeverNull(final AnalysedRecord analysedRecord, final Optional<AnalysedOptionalCollection> optionalCollection) {
        super(Constants.Claims.BUILDER_SET_TO_NULL, analysedRecord);
        this.optionalCollection = optionalCollection;
    }

    @Override
    public int specificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        final String methodName = "set" + analysedComponent.nameFirstLetterCaps() + "ToNull";
        final MethodSpec.Builder builder = analysedRecord.builderArtifact().createMethod(methodName, claimableOperation)
            .addModifiers(Modifier.FINAL, Modifier.PUBLIC);
        if (optionalCollection.isEmpty()) {
            builder.addStatement(
                "this.$L.clear()",
                analysedComponent.name()
            );
        } else {
            builder.addStatement(
                "this.$L($T.empty())",
                analysedComponent.name(),
                CommonsConstants.Names.OPTIONAL
            );
        }

        builder.addStatement("return this")
            .addJavadoc(
                "Sets the value of $L to an empty collection.\nThis is because {@code null} collections become empty.\n",
                analysedComponent.name()
            )
            .returns(analysedRecord.builderArtifact().className());
        AnnotationSupplier.addGeneratedAnnotation(builder, this);

        return true;
    }
}

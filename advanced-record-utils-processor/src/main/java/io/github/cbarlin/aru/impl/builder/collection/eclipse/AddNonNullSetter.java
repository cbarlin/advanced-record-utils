package io.github.cbarlin.aru.impl.builder.collection.eclipse;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.Constants.Names.ITERABLE;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.types.dependencies.EclipseCollectionComponent;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public final class AddNonNullSetter extends EclipseComponentVisitor {

    public AddNonNullSetter() {
        super(Claims.CORE_BUILDER_SETTER);
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.settings().prism().builderOptions().buildNullCollectionToEmpty());
    }

    @Override
    public int innerSpecificity() {
        return 2;
    }

    @Override
    public boolean innerVisitComponent(final EclipseCollectionComponent ecc) {
        if (ecc.isIntendedConstructorParam()) {
            setIterable(ecc);
            return true;
        }
        return false;
    }

    private void setIterable(final EclipseCollectionComponent ecc) {
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(ITERABLE, ecc.unNestedPrimaryTypeName());
        final String name = ecc.name();
        final MethodSpec.Builder method = ecc.builderArtifact()
            .createMethod(ecc.name(), claimableOperation)
            .returns(ecc.builderArtifact().className())
            .addParameter(
                ParameterSpec.builder(paramTypeName, ecc.name(), Modifier.FINAL)
                    .addJavadoc("The replacement value")
                    .build()
            )
            .addAnnotation(CommonsConstants.Names.NON_NULL);

        if (!Boolean.FALSE.equals(builderOptions.nullReplacesNotNull())) {
            method.addJavadoc("\n<p>\n")
                  .addJavadoc("Supplying a null value will set the current value to an empty collection")
                  .addStatement("this.$L.clear()", name)
                  .beginControlFlow("if ($T.nonNull($L))", OBJECTS, name)
                  .addStatement("this.$L.addAllIterable($L)", name, name)
                  .endControlFlow();
        } else {
            method.addJavadoc("\n<p>\n")
                  .addJavadoc("Supplying a null value won't replace a set value (but an empty collection will)")
                  .beginControlFlow("if ($T.nonNull($L))", OBJECTS, name)
                  .addStatement("this.$L.clear()", name)
                  .addStatement("this.$L.addAllIterable($L)", name, name)
                  .endControlFlow();
        }
   
        method.addStatement("return this");
        AnnotationSupplier.addGeneratedAnnotation(method, this);
    }
}

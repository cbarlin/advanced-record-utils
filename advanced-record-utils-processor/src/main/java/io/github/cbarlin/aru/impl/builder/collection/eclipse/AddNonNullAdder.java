package io.github.cbarlin.aru.impl.builder.collection.eclipse;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.types.dependencies.EclipseCollectionComponent;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public final class AddNonNullAdder extends EclipseComponentVisitor {

    public AddNonNullAdder() {
        super(Claims.CORE_BUILDER_SINGLE_ITEM_ADDER);
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
    public boolean innerVisitComponent(EclipseCollectionComponent ecc) {
        if (ecc.isIntendedConstructorParam()) {
            final String name = ecc.name();
            final var singularType = ecc.unNestedPrimaryTypeName();
            final String methodName = addNameMethodName(ecc);
            final ParameterSpec param = ParameterSpec.builder(singularType, name, Modifier.FINAL)
                .addJavadoc("A singular instance to be added to the collection")
                .addAnnotation(NULLABLE)
                .build();
            final MethodSpec.Builder method = ecc.builderArtifact()
                .createMethod(methodName, claimableOperation)
                .returns(ecc.builderArtifact().className())
                .addAnnotation(NOT_NULL)
                .addParameter(param)
                .addJavadoc("Add a singular {@link $T} to the collection for the field {@code $L}", singularType, name)
                .addStatement("this.$L.add($L)", name, name)
                .addStatement("return this");

            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }
}

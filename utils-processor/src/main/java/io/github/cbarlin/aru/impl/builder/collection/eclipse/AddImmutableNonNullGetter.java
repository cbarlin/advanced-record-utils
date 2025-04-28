package io.github.cbarlin.aru.impl.builder.collection.eclipse;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.types.dependencies.EclipseCollectionComponent;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class AddImmutableNonNullGetter extends EclipseComponentVisitor {

    public AddImmutableNonNullGetter() {
        super(Claims.CORE_BUILDER_GETTER);
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.settings().prism().builderOptions().buildNullCollectionToEmpty());
    }

    @Override
    public int innerSpecificity() {
        return 3;
    }

    @Override
    public boolean innerVisitComponent(final EclipseCollectionComponent ecc) {
        if (ecc.isIntendedConstructorParam() && ecc.isImmutableCollection()) {
            final String name = ecc.name();
            final MethodSpec.Builder method = ecc.builderArtifact()
                .createMethod(name, claimableOperation)
                .addAnnotation(NON_NULL)
                .returns(ecc.typeName())
                .addJavadoc("Returns the current value of {@code $L}", name)
                .addStatement("return this.$L.toImmutable()", name);

            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }
}

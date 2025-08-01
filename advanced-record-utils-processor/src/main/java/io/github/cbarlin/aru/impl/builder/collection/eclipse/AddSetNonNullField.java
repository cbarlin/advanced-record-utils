package io.github.cbarlin.aru.impl.builder.collection.eclipse;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.ECLIPSE_COLLECTIONS__MUTABLE_SET;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.types.dependencies.EclipseCollectionComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.FieldSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public final class AddSetNonNullField extends EclipseComponentVisitor {

    private static final ClassName SETS_FACTORY = ClassName.get("org.eclipse.collections.api.factory", "Sets");

    public AddSetNonNullField() {
        super(Claims.CORE_BUILDER_FIELD);
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
        if (ecc.isIntendedConstructorParam() && ecc.isSet()) {
            final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(ECLIPSE_COLLECTIONS__MUTABLE_SET, ecc.unNestedPrimaryTypeName());
            final FieldSpec field = FieldSpec.builder(paramTypeName, ecc.name(), Modifier.FINAL, Modifier.PRIVATE)
                .addAnnotation(NON_NULL)
                .addJavadoc("Internal mutable set for {@code $L}", ecc.name())
                .initializer("$T.mutable.empty()", SETS_FACTORY)
                .build();
            ecc.builderArtifact().addField(field);
            return true;
        }
        return false;
    }
}

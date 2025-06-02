package io.github.cbarlin.aru.impl.builder.collection.fastutils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.FASTUTILS__BOOLEAN_COLLECTION;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.types.dependencies.fastutils.FastUtilsCollectionComponent;
import io.micronaut.sourcegen.javapoet.FieldSpec;

@ServiceProvider
public class AddField extends RecordVisitor {

    public AddField() {
        super(Claims.CORE_BUILDER_FIELD);
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return OptionalClassDetector.doesDependencyExist(FASTUTILS__BOOLEAN_COLLECTION);
    }

    @Override
    public int specificity() {
        return 5;
    }

    @Override
    protected boolean visitComponentImpl(final AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof final FastUtilsCollectionComponent fcc && fcc.isIntendedConstructorParam()) {
            final FieldSpec field = FieldSpec.builder(fcc.typeName(), fcc.name(), Modifier.PRIVATE)
                .addAnnotation(NON_NULL)
                .addJavadoc("Internal mutable list for {@code $L}", fcc.name())
                .initializer("new $T()", fcc.typeName())
                .build();
            fcc.builderArtifact().addField(field);
            return true;
        }
        return false;
    }
}

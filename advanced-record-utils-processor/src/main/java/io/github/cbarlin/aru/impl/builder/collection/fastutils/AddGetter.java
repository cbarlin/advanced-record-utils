package io.github.cbarlin.aru.impl.builder.collection.fastutils;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.FASTUTILS__BOOLEAN_COLLECTION;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class AddGetter extends CollectionRecordVisitor {

    public AddGetter() {
        super(Claims.CORE_BUILDER_GETTER);
    }

    @Override
    protected int collectionSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitCollectionComponent(final AnalysedCollectionComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final String name = analysedComponent.name();
            final MethodSpec.Builder method = analysedComponent.builderArtifact()
                .createMethod(name, claimableOperation, analysedComponent.element())
                .returns(analysedComponent.typeName())
                .addAnnotation(NON_NULL)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Returns the current value of {@code $L}, ensuring that null is never returned\n", name)
                .addStatement("return $T.requireNonNullElse(this.$L, $T.of())", OBJECTS, name, analysedComponent.typeName());
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord target) {
        return OptionalClassDetector.doesDependencyExist(FASTUTILS__BOOLEAN_COLLECTION) && (!Boolean.FALSE.equals(target.settings().prism().builderOptions().buildNullCollectionToEmpty()));
    }
}

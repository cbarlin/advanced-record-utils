package io.github.cbarlin.aru.core.impl.visitors.builder.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.HASH_SET;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.collection.SetRecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class AddSetNeverNullGetter extends SetRecordVisitor {

    public AddSetNeverNullGetter() {
        super(Claims.CORE_BUILDER_GETTER);
    }

    @Override
    protected int setSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitSetComponent(AnalysedCollectionComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final String name = analysedComponent.name();
            final MethodSpec.Builder method = analysedComponent.builderArtifact()
                .createMethod(name, claimableOperation, analysedComponent.element())
                .returns(analysedComponent.typeName())
                .addAnnotation(NON_NULL)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Returns the current value of {@code $L}, ensuring that null is never returned\n", name)
                .addStatement("return $T.requireNonNullElse(this.$L, new $T<>())", OBJECTS, name, HASH_SET);
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return !Boolean.FALSE.equals(analysedRecord.settings().prism().builderOptions().buildNullCollectionToEmpty());
    }

}

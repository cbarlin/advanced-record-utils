package io.github.cbarlin.aru.impl.builder.collection.fastutils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NOT_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.FASTUTILS__BOOLEAN_COLLECTION;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.types.dependencies.fastutils.FastUtilsCollectionComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public class AddSingleAdder extends CollectionRecordVisitor {

    public AddSingleAdder() {
        super(Claims.CORE_BUILDER_SINGLE_ITEM_ADDER);
    }

    @Override
    protected int collectionSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitCollectionComponent(final AnalysedCollectionComponent analysedCollectionComponent) {
        if (analysedCollectionComponent instanceof final FastUtilsCollectionComponent acc && acc.isIntendedConstructorParam()) {
            final String name = acc.name();
            final TypeName singularType = acc.unNestedPrimaryTypeName();
            final String methodName = addNameMethodName(acc);
            final ClassName builderClassName = acc.builderArtifact().className();
            final ParameterSpec param = ParameterSpec.builder(singularType, name, Modifier.FINAL)
                .addJavadoc("A singular instance to be added to the collection")
                .addAnnotation(NULLABLE)
                .build();
            
            final MethodSpec.Builder method = acc.builderArtifact().createMethod(methodName, claimableOperation)
                .returns(builderClassName)
                .addAnnotation(NOT_NULL)
                .addParameter(param)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Add a singular {@link $T} to the collection for the field {@code $L}", singularType, name)
                .addStatement("this.$L.add($L)", acc.name(), acc.name())
                .addStatement("return this");

            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord target) {
        return OptionalClassDetector.doesDependencyExist(FASTUTILS__BOOLEAN_COLLECTION);
    }

}

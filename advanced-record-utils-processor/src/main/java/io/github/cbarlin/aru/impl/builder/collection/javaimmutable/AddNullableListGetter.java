package io.github.cbarlin.aru.impl.builder.collection.javaimmutable;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuiltCollectionType;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.collection.ListRecordVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@ServiceProvider
public class AddNullableListGetter extends ListRecordVisitor {

    public AddNullableListGetter() {
        super(Claims.CORE_BUILDER_GETTER);
    }

    @Override
    protected int listSpecificity() {
        return 1;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return BuiltCollectionType.JAVA_IMMUTABLE.name().equals(analysedRecord.settings().prism().builderOptions().builtCollectionType());
    }

    @Override
    protected boolean visitListComponent(AnalysedCollectionComponent analysedComponent) {
        if(analysedComponent.isIntendedConstructorParam()) {
            final String name = analysedComponent.name();
            final MethodSpec.Builder method = analysedComponent.builderArtifact()
                .createMethod(name, claimableOperation, analysedComponent.element())
                .returns(analysedComponent.typeName())
                .addAnnotation(NULLABLE)
                .addJavadoc("Returns the current value of {@code $L}", name)
                .beginControlFlow("if ($T.isNull(this.$L))", OBJECTS, name)
                .addStatement("return null")
                .endControlFlow()
                .addStatement("return this.$L\n.stream()\n.filter($T::nonNull)\n.toList()", name, OBJECTS);

            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;
        }
        return false;
    }
}

package io.github.cbarlin.aru.impl.builder.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;

import java.util.Optional;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.CollectionRecordVisitor;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandler;
import io.github.cbarlin.aru.impl.types.collection.CollectionHandlerHolder;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class AddSetter extends CollectionRecordVisitor {

    public AddSetter() {
        super(CommonsConstants.Claims.CORE_BUILDER_SETTER);
    }

    @Override
    public int collectionSpecificity() {
        return 5;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord target) {
        return true;
    }

    @Override
    protected boolean visitCollectionComponent(final AnalysedCollectionComponent ac) {
        if (ac.isIntendedConstructorParam()) {
            final Optional<CollectionHandler> handlerOptional = CollectionHandlerHolder.COLLECTION_HANDLERS
                .stream()
                .filter(c -> c.canHandle(ac))
                .findFirst();
            if (handlerOptional.isPresent()) {
                final CollectionHandler handler = handlerOptional.get();
                final var settings = ac.settings().prism().builderOptions();
                final boolean nonNull = !Boolean.FALSE.equals(settings.buildNullCollectionToEmpty());
                final boolean immutable = !"AUTO".equals(settings.builtCollectionType());
                final TypeName innerType = ac.unNestedPrimaryTypeName();
                final MethodSpec.Builder method = ac.builderArtifact()
                    .createMethod(ac.name(), claimableOperation, ac.element());
                final boolean nullReplacesNonNull = (!Boolean.FALSE.equals(ac.settings().prism().builderOptions().nullReplacesNotNull()));
                
                final String name = ac.name();

                populateStartOfMethod(ac, method, nullReplacesNonNull, name);

                if (nonNull && immutable) {
                    handler.writeNonNullImmutableSetter(ac, method, innerType, nullReplacesNonNull);
                } else if (nonNull) {
                    handler.writeNonNullAutoSetter(ac, method, innerType, nullReplacesNonNull);
                } else if (immutable) {
                    handler.writeNullableImmutableSetter(ac, method, innerType, nullReplacesNonNull);
                } else {
                    handler.writeNullableAutoSetter(ac, method, innerType, nullReplacesNonNull);
                }

                method.addStatement("return this");
                AnnotationSupplier.addGeneratedAnnotation(method, this);

                return true;
            }
        }
        return false;
    }

    private void populateStartOfMethod(final AnalysedCollectionComponent ac, final MethodSpec.Builder method,
            final boolean nullReplacesNonNull, final String name) {
        final ParameterSpec param = ParameterSpec.builder(ac.typeName(), name, Modifier.FINAL)
            .addJavadoc("The replacement value")
            .addAnnotation(NULLABLE)
            .build();

        method.addJavadoc("Updates the value of {@code $L}", name)
            .returns(ac.builderArtifact().className())
            .addParameter(param)
            .addAnnotation(CommonsConstants.Names.NON_NULL)
            .addModifiers(Modifier.PUBLIC);
        
        if (nullReplacesNonNull) {
            method.addJavadoc("\n<p>\n")
                .addJavadoc("Supplying a null value will set the current value to null");
        } else {
            method.addJavadoc("\n<p>\n")
                .addJavadoc("Supplying a null value won't replace a set value");
        }
    }
}

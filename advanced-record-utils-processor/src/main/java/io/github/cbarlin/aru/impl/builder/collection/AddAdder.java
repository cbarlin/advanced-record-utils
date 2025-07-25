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
public final class AddAdder extends CollectionRecordVisitor {

    public AddAdder() {
        super(CommonsConstants.Claims.CORE_BUILDER_SINGLE_ITEM_ADDER);
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
                final String methodName = addNameMethodName(ac);
                final MethodSpec.Builder method = ac.builderArtifact()
                    .createMethod(methodName, claimableOperation, ac.element());
                final boolean nullReplacesNonNull = (!Boolean.FALSE.equals(ac.settings().prism().builderOptions().nullReplacesNotNull()));
                
                final String name = ac.name();

                final ParameterSpec param = ParameterSpec.builder(innerType, name, Modifier.FINAL)
                    .addJavadoc("A singular instance to be added to the collection")
                    .addAnnotation(NULLABLE)
                    .build();

                method.addJavadoc("Add a singular {@link $T} to the collection for the field {@code $L}", innerType, name)
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

                if (nonNull && immutable) {
                    handler.writeNonNullImmutableAddSingle(ac, method, innerType);
                } else if (nonNull) {
                    handler.writeNonNullAutoAddSingle(ac, method, innerType);
                } else if (immutable) {
                    handler.writeNullableImmutableAddSingle(ac, method, innerType);
                } else {
                    handler.writeNullableAutoAddSingle(ac, method, innerType);
                }

                method.addStatement("return this");
                AnnotationSupplier.addGeneratedAnnotation(method, this);

                return true;
            }
        }
        return false;
    }
}

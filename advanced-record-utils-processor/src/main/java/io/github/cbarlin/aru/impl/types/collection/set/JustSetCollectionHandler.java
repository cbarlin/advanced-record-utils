package io.github.cbarlin.aru.impl.types.collection.set;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.HASH_SET;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.SET;

import javax.lang.model.element.ElementKind;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class JustSetCollectionHandler extends SetCollectionHandler {

    public JustSetCollectionHandler() {
        super(SET, HASH_SET);
    }

    @Override
    public boolean canHandle(final AnalysedComponent component) {
        if (component.typeName() instanceof final ParameterizedTypeName ptn && ptn.rawType.equals(SET)) {
            final TypeName innerTypeName = ptn.typeArguments.get(0);
            return !(
                innerTypeName instanceof final ClassName innerClassName && 
                APContext.elements().getTypeElement(innerClassName.toString()).getKind().equals(ElementKind.ENUM)
            );
        }
        
        return false;
    }
}

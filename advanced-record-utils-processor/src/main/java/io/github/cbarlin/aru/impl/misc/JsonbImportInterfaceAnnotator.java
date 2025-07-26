package io.github.cbarlin.aru.impl.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.TypeElement;

import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.types.ProcessingTarget;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.Constants.Names;
import io.github.cbarlin.aru.impl.wiring.BasePerInterfaceScope;
import io.github.cbarlin.aru.prism.prison.JsonBPrism;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import io.micronaut.sourcegen.javapoet.ClassName;
import jakarta.inject.Singleton;

@Singleton
@BasePerInterfaceScope
@RequiresProperty(value = "addJsonbImportAnnotation", equalTo = "true")
public final class JsonbImportInterfaceAnnotator extends InterfaceVisitor {

    public JsonbImportInterfaceAnnotator(final AnalysedInterface analysedInterface) {
        super(Claims.MISC_AVAJE_JSONB_IMPORT, analysedInterface);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitInterfaceImpl() {
        if (JsonBPrism.isPresent(analysedInterface.typeElement())) {
            // Already done
            return true;
        }
        final AnnotationSpec.Builder jsonbAnnotation = AnnotationSpec.builder(Names.AVAJE_JSONB_IMPORT)
            .addMember("value", "{$T.class}", analysedInterface.className());

        final StringBuilder formatBuilder = new StringBuilder("{");
        final List<Object> params = new ArrayList<>();

        for(final ProcessingTarget target : analysedInterface.implementingTypes()) {
            final TypeElement te = target.typeElement();
            if (Objects.isNull(te)) {
                continue;
            }
            final ClassName othClassName = ClassName.get(te);
            formatBuilder.append("\n\t@$T(type = $T.class),");
            params.add(Names.AVAJE_JSONB_SUBTYPE);
            params.add(othClassName);
        }

        formatBuilder.append('\n').append('}');
        jsonbAnnotation.addMember("subtypes", formatBuilder.toString(), params.toArray());
        
        analysedInterface.utilsClass()
            .builder()
            .addAnnotation(jsonbAnnotation.build());
        return true;
    }
    
}

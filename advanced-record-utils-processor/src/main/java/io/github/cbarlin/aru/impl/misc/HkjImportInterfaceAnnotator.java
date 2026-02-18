package io.github.cbarlin.aru.impl.misc;

import io.avaje.inject.RequiresProperty;
import io.github.cbarlin.aru.core.types.AnalysedInterface;
import io.github.cbarlin.aru.core.visitors.InterfaceVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.Constants.Names;
import io.github.cbarlin.aru.impl.wiring.BasePerInterfaceScope;
import io.micronaut.sourcegen.javapoet.AnnotationSpec;
import jakarta.inject.Singleton;

import javax.lang.model.element.Modifier;

@Singleton
@BasePerInterfaceScope
@RequiresProperty(value = "addHigherKindedJImportAnnotation", equalTo = "true")
public final class HkjImportInterfaceAnnotator extends InterfaceVisitor {

    public HkjImportInterfaceAnnotator(final AnalysedInterface analysedInterface) {
        super(Claims.MISC_HKJ_IMPORT, analysedInterface);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected boolean visitInterfaceImpl() {
        if (!analysedInterface.typeElement().getModifiers().contains(Modifier.SEALED)) {
            return false;
        }
        analysedInterface.utilsClass()
                .builder()
                .addAnnotation(
                        AnnotationSpec.builder(Names.HKJ_IMPORT)
                                .addMember("value", "{$T.class}", analysedInterface.className())
                                .build()
                );
        return true;
    }
}

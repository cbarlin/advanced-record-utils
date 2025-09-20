package io.github.cbarlin.aru.core.types;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.micronaut.sourcegen.javapoet.ClassName;

public final class AnalysedRecord extends AnalysedType {
    private final ExecutableElement canonicalConstructor;
    private final ExecutableElement intendedConstructor;
    private final TypeElement intendedTypeElement;

    //#region constructor

    public AnalysedRecord(
        final TypeElement element,
        final UtilsProcessingContext context,
        final AdvRecUtilsSettings settings,
        final TypeElement intendedTypeElement,
        final ExecutableElement canonicalConstructor,
        final ExecutableElement intendedConstructor
    ) {
        super(element, context, settings);
        this.canonicalConstructor = canonicalConstructor;
        this.intendedConstructor = intendedConstructor;
        this.intendedTypeElement = intendedTypeElement;
    }

    //#endregion
    
    //#region Getters etc
    public ExecutableElement canonicalConstructor() {
        return canonicalConstructor;
    }

    public ToBeBuilt builderArtifact() {
        return utilsClassChildClass(settings.builderClassName(), CommonsConstants.Claims.CORE_BUILDER_CLASS);
    }

    public ClassName intendedType() {
        return ClassName.get(intendedTypeElement);
    }

    @Override
    public TypeElement intentedTypeElement() {
        return intendedTypeElement;
    }

    public ExecutableElement intendedConstructor() {
        return this.intendedConstructor;
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    //#endregion
}

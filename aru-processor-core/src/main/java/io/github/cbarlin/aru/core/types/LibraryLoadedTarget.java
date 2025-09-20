package io.github.cbarlin.aru.core.types;

import java.util.Objects;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.jspecify.annotations.NonNull;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.artifacts.GenerationArtifact;
import io.github.cbarlin.aru.core.artifacts.PreBuilt;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * Represents a target that was referenced that already has a *Utils generated (e.g. it came from a library)
 */
public final class LibraryLoadedTarget implements ProcessingTarget {

    private static TypeElement useInterfaceDefaultClass;
    private final PreBuilt preBuilt;
    private final TypeElement typeElement;
    private final TypeElement intendedTypeElement;

    public LibraryLoadedTarget(final PreBuilt preBuilt, final TypeElement typeElement) {
        this.preBuilt = preBuilt;
        this.typeElement = typeElement;
        if(
            Objects.nonNull(preBuilt.prism().settings().useInterface()) &&
            (!APContext.types().isSameType(defaultClassUseInterface(), preBuilt.prism().settings().useInterface()))
        ) {
            intendedTypeElement = Objects.requireNonNullElse(APContext.asTypeElement(preBuilt.prism().settings().useInterface()), typeElement);
        } else {
            intendedTypeElement = typeElement;
        }
    }

    @Override
    @NonNull
    public PreBuilt utilsClass() {
        return this.preBuilt;
    }

    @Override
    public TypeElement typeElement() {
        return this.typeElement;
    }

    @Override
    public TypeElement intentedTypeElement() {
        return intendedTypeElement;
    }

    public ClassName intendedType() {
        return ClassName.get(intendedTypeElement);
    }

    @Override
    public AdvancedRecordUtilsPrism prism() {
        return utilsClass().prism().settings();
    }
    
    public GenerationArtifact<?> builderArtifact() {
        final String builderName = prism().builderOptions().builderName();
        return Objects.requireNonNull(preBuilt.children().get(builderName), "Target must have a builder");
    }

    private static TypeMirror defaultClassUseInterface() {
        if (Objects.isNull(useInterfaceDefaultClass)) {
            useInterfaceDefaultClass = APContext.elements().getTypeElement("io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DEFAULT");
            Objects.requireNonNull(useInterfaceDefaultClass);
        }
        return useInterfaceDefaultClass.asType();
    }
}

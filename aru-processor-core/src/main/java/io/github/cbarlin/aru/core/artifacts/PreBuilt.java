package io.github.cbarlin.aru.core.artifacts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;

import io.github.cbarlin.aru.core.APContext;
import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsGeneratedPrism;
import io.micronaut.sourcegen.javapoet.ClassName;

public final class PreBuilt implements GenerationArtifact<PreBuilt> {

    private final ClassName className;
    private final Map<String, PreBuilt> children;
    private final AdvancedRecordUtilsGeneratedPrism prism;

    public PreBuilt(final TypeElement target) {
        this.className = ClassName.get(target);
        this.prism = AdvancedRecordUtilsGeneratedPrism.getInstanceOn(target);
        if (Objects.isNull(this.prism)) {
            APContext.messager().printMessage(
                Diagnostic.Kind.ERROR,
                "Requested build of a PreBuilt (library loaded) element but cannot find the prism",
                target
            );
        }
        final Map<String, PreBuilt> children = new HashMap<>();
        target.getEnclosedElements().forEach(enc -> {
            if (enc instanceof final TypeElement typeElement) {
                children.put(ClassName.get(typeElement).simpleName(), new PreBuilt(typeElement, this.prism));
            }
        });
        this.children = Map.copyOf(children);

    }

    PreBuilt(final TypeElement target, final AdvancedRecordUtilsGeneratedPrism prism) {
        this.prism = prism;
        this.className = ClassName.get(target);
        final Map<String, PreBuilt> children = new HashMap<>();
        target.getEnclosedElements().forEach(enc -> {
            if (enc instanceof final TypeElement typeElement) {
                children.put(ClassName.get(typeElement).simpleName(), new PreBuilt(typeElement, prism));
            }
        });
        this.children = Map.copyOf(children);

    }

    public PreBuilt(final DeclaredType declaredType) {
        this((TypeElement) declaredType.asElement());
    }

    @Override
    public ClassName className() {
        return className;
    }

    @Nullable
    public PreBuilt childArtifact(final String generatedCodeName, final ClaimableOperation claimableOperation) {
        return children.get(generatedCodeName);
    }

    public Map<String, PreBuilt> children() {
        return Map.copyOf(this.children);
    }

    public AdvancedRecordUtilsGeneratedPrism prism() {
        return this.prism;
    }

}

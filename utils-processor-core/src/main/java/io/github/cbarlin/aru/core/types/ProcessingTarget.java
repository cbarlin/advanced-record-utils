package io.github.cbarlin.aru.core.types;

import javax.lang.model.element.TypeElement;

import org.jspecify.annotations.Nullable;

import io.github.cbarlin.aru.core.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.core.ClaimableOperation;
import io.github.cbarlin.aru.core.artifacts.GenerationArtifact;

import io.micronaut.sourcegen.javapoet.ClassName;

/**
 * A target that is relevant to our processing context. It may be one of the following:
 * <p>
 * <ul>
 *  <li>An interface, which lists its implementing records. See {@link AnalysedInterface}</li>
 *  <li>A record in the current compilation run. See {@link AnalysedRecord}</li>
 *  <li>A reference to a record or interface in a library that already has a Utils. See {@link LibraryLoadedTarget}</li>
 * </ul>
 */
// Ignore the generics, they aren't relevant in practice
@SuppressWarnings({"java:S1452"})
public sealed interface ProcessingTarget permits LibraryLoadedTarget, AnalysedType {
    
    public TypeElement typeElement();

    public GenerationArtifact<?> utilsClass();

    @Nullable
    public default GenerationArtifact<?> utilsClassChild(final String generatedName, final ClaimableOperation operation) {
        return utilsClass().childArtifact(generatedName, operation);
    }

    public default ClassName utilsClassName() {
        return utilsClass().className();
    }

    public AdvancedRecordUtilsPrism prism();

    public GenerationArtifact builderArtifact();
}

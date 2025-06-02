package io.github.cbarlin.aru.impl.types.dependencies.fastutils;

import static io.github.cbarlin.aru.impl.types.dependencies.DependencyClassNames.FASTUTILS__BOOLEAN_COLLECTION;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.DeclaredType;

import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.micronaut.sourcegen.javapoet.TypeName;

public class FastUtilsCollectionComponent extends AnalysedCollectionComponent {

    private final TypeName mutableVersion;

    public FastUtilsCollectionComponent(final RecordComponentElement element, final AnalysedRecord parentRecord, final boolean isIntendedConstructorParam, final UtilsProcessingContext utilsProcessingContext) {
        super(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext);
        this.mutableVersion = findMutableVersion(typeName, element);
    }

    private static TypeName findMutableVersion(final TypeName incoming, final RecordComponentElement element) {
        final DeclaredType dt = (DeclaredType) element.asType();
        final var targetElement = APContext.asTypeElement(dt);
        if (ElementKind.INTERFACE.equals(targetElement.getKind()) || (ElementKind.CLASS.equals(targetElement.getKind()) && element.getModifiers().contains(Modifier.ABSTRACT))) {
            // OK, work out what this is!
            if (OptionalClassDetector.checkSameOrSubType(element, FASTUTILS__BOOLEAN_COLLECTION)) {

            }
        }
        return TypeName.get(dt);
    }

    public TypeName mutableVersion() {
        return mutableVersion;
    }

}

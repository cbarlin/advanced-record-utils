package io.github.cbarlin.aru.impl.types;

import static io.github.cbarlin.aru.impl.Constants.Names.TYPE_ALIAS;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.jspecify.annotations.Nullable;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.UtilsProcessingContext;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.types.ComponentAnalyser;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public class TypeAliasAnalyser implements ComponentAnalyser {

    @Override
    public int specificity() {
        return 2;
    }

    @Override
    public @Nullable AnalysedComponent analyseComponent(
        final RecordComponentElement element,
        final AnalysedRecord parentRecord,
        final boolean isIntendedConstructorParam,
        final UtilsProcessingContext utilsProcessingContext
    ) {
        if (OptionalClassDetector.checkSameOrSubType(element, TYPE_ALIAS)) {
            final Types typeUtils = APContext.types();

            final Queue<TypeMirror> interfaceQueue = new LinkedList<>();
            final Set<TypeName> seen = new HashSet<>();
            interfaceQueue.addAll(APContext.asTypeElement(element.asType()).getInterfaces());

            while (!interfaceQueue.isEmpty()) {
                final TypeMirror currentIfaceMirror = interfaceQueue.poll();
                if (!seen.add(TypeName.get(currentIfaceMirror))) {
                    continue; // already processed
                }
                final TypeMirror erasedIface = typeUtils.erasure(currentIfaceMirror);

                if (currentIfaceMirror instanceof final DeclaredType declaredType && TYPE_ALIAS.equals(TypeName.get(erasedIface))) {
                    // Found TypeAlias!
                    if (!declaredType.getTypeArguments().isEmpty()) {
                        final TypeName aliasFor = TypeName.get(declaredType.getTypeArguments().get(0));
                        return new TypeAliasComponent(element, parentRecord, isIntendedConstructorParam, utilsProcessingContext, aliasFor);
                    } else {
                        APContext.messager().printError("Found TypeAlias but it has no type arguments?", element);
                        return null;
                    }
                }

                // Add super-interfaces of the current interface to the queue for further exploration
                if (typeUtils.asElement(currentIfaceMirror) instanceof final TypeElement ifaceElement) {
                     interfaceQueue.addAll(ifaceElement.getInterfaces());
                }
            }

            // If we reach here, TypeAlias was not found correctly in the hierarchy
            APContext.messager().printWarning(
                "Read type as a subtype of TypeAlias, but could not extract the aliased type from the hierarchy. Ignoring the aliasing for this element",
                element
            );
        }
        return null;
    }
}

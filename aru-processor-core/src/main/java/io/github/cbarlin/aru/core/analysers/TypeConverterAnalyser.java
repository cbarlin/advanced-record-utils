package io.github.cbarlin.aru.core.analysers;

import io.avaje.inject.Component;
import io.avaje.inject.Priority;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.AdvRecUtilsSettings;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.OptionalClassDetector;
import io.github.cbarlin.aru.core.types.AnalysedTypeConverter;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.github.cbarlin.aru.prism.prison.TypeConverterPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@CoreGlobalScope
@Priority(5)
public final class TypeConverterAnalyser implements TargetAnalyser {

    private static final Set<Modifier> REQUIRED_MODIFIERS_ON_METHOD = Set.of(Modifier.PUBLIC, Modifier.STATIC);
    private static final Set<Modifier> REQUIRED_MODIFIERS_ON_CLASS = Set.of(Modifier.PUBLIC);

    @Override
    public TargetAnalysisResult analyse(Element element, Optional<AdvRecUtilsSettings> parentSettings) {

        return Optional.of(element)
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .filter(TypeConverterPrism::isPresent)
                .flatMap(TypeConverterAnalyser::fromExecutableElement)
                .orElse(TargetAnalysisResult.EMPTY_RESULT);
    }

    private static Optional<TargetAnalysisResult> fromExecutableElement(final ExecutableElement executableElement) {
        final TypeConverterPrism prism = TypeConverterPrism.getInstanceOn(executableElement);
        final boolean validateReturn = Objects.nonNull(prism) && !Boolean.TRUE.equals(prism.permitReturnTypeWhichMayResultInTooManyMethods());
        return Optional.of(executableElement)
                .filter(TypeConverterAnalyser::validateExecutableElement)
                .filter(TypeConverterAnalyser::validateEnclosingClasses)
                .filter(e -> (!validateReturn) || validateReturnType(e))
                .map(TypeConverterAnalyser::create)
                .map(atc -> new TargetAnalysisResult(Optional.empty(), Set.of(), false, Set.of(atc)));
    }

    private static AnalysedTypeConverter create(final ExecutableElement executableElement) {
        final TypeName resultingType = TypeName.get(executableElement.getReturnType());
        final ClassName className = ClassName.get((TypeElement) executableElement.getEnclosingElement());
        final String methodName = executableElement.getSimpleName().toString();
        final List<ParameterSpec> specs = executableElement.getParameters()
                .stream()
                .map(ParameterSpec::get)
                .toList();
        return new AnalysedTypeConverter(resultingType, className, methodName, specs, executableElement);
    }

    private static boolean validateReturnType(final ExecutableElement executableElement) {
        final TypeName retType = TypeName.get(executableElement.getReturnType());
        if (retType instanceof final ParameterizedTypeName ptn) {
            return validateReturnType(ptn, executableElement);
        } else if (retType instanceof final ClassName cn) {
            return validateReturnType(cn, executableElement);
        } else {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "TypeConverter annotation ignored as it returns a primitive, which you probably don't want. If you do wish to enable this, add `permitReturnTypeWhichMayResultInTooManyMethods = true` to the method", executableElement);
            return false;
        }
    }

    private static boolean validateReturnType(final ParameterizedTypeName ptn, final ExecutableElement executableElement) {
        if (ptn.typeArguments.size() == 1 && (CommonsConstants.Names.OPTIONAL.equals(ptn.rawType) || OptionalClassDetector.checkSameOrSubType(ptn.rawType, CommonsConstants.Names.COLLECTION))) {
            if (ptn.typeArguments.getFirst() instanceof ClassName cn) {
                return validateReturnType(cn, executableElement);
            }
        }
        return true;
    }

    private static boolean validateReturnType(final ClassName className, final ExecutableElement executableElement) {
        final String canonicalName = className.canonicalName();
        if (canonicalName.startsWith("java") || canonicalName.startsWith("jakarta")) {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "TypeConverter annotation ignored as it returns a Java built-in type, which you probably don't want. If you do wish to enable this, add `permitReturnTypeWhichMayResultInTooManyMethods = true` to the method", executableElement);
            return false;
        }
        return true;
    }

    private static boolean validateExecutableElement(final ExecutableElement executableElement) {
        if (!executableElement.getModifiers().containsAll(REQUIRED_MODIFIERS_ON_METHOD)) {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "TypeConverter annotation needs to be on a 'public static' method", executableElement);
            return false;
        }
        if (executableElement.getSimpleName().isEmpty()) {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "TypeConverter annotation needs to be on a named method", executableElement);
            return false;
        }
        return true;
    }

    private static boolean validateEnclosingClasses(final ExecutableElement executableElement) {
        final Element enclosingElement = executableElement.getEnclosingElement();
        if ((!(enclosingElement instanceof TypeElement enclosingType)) || enclosingType.getQualifiedName().isEmpty()) {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "TypeConverter annotation " + executableElement.getSimpleName() + " needs to be inside a named class, and this isn't a named class", enclosingElement);
            return false;
        }
        return validateUpwardPublicClasses(enclosingType, executableElement);
    }

    private static boolean validateUpwardPublicClasses(final TypeElement typeElement, final ExecutableElement executableElement) {
        if (!typeElement.getModifiers().containsAll(REQUIRED_MODIFIERS_ON_CLASS)) {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "TypeConverter annotation " + executableElement.getSimpleName() + " needs to be inside a public class structure, and this isn't public", typeElement);
            return false;
        }
        final @Nullable Element parent = typeElement.getEnclosingElement();
        if (parent instanceof PackageElement) {
            return true;
        } else if (parent instanceof final TypeElement te) {
            return validateUpwardPublicClasses(te, executableElement);
        } else {
            APContext.messager().printMessage(Diagnostic.Kind.ERROR, "TypeConverter annotation needs to be in a named class", executableElement);
            return false;
        }
    }
}

package io.github.cbarlin.aru.core.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import io.github.cbarlin.aru.annotations.TypeConverter;
import io.github.cbarlin.aru.core.APContext;

import io.avaje.prism.GeneratePrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@GeneratePrism(value = TypeConverter.class, publicAccess = true)
public class AnalysedTypeConverter {
    private static final Map<TypeElement, List<AnalysedTypeConverter>> CACHE = new HashMap<>();

    private final ClassName referenceClassName;
    private final String methodName;
    private final List<ParameterSpec> parameters;
    private final ExecutableElement executableElement;

    private AnalysedTypeConverter(final ExecutableElement executableElement) {
        this.referenceClassName = ClassName.get((TypeElement) executableElement.getEnclosingElement());
        this.methodName = executableElement.getSimpleName().toString();
        final List<ParameterSpec> params = new ArrayList<>();
        for (final VariableElement parameter : executableElement.getParameters()) {
            params.add(
                ParameterSpec.builder(TypeName.get(parameter.asType()), parameter.getSimpleName().toString(), Modifier.FINAL)
                    .build()  
            );
        }
        this.parameters = List.copyOf(params);
        this.executableElement = executableElement;
    }

    public static List<AnalysedTypeConverter> getTypeConverters(final TypeElement typeElement) {
        return CACHE.computeIfAbsent(typeElement, AnalysedTypeConverter::getTypeConvertersImpl);
    }

    private static List<AnalysedTypeConverter> getTypeConvertersImpl(final TypeElement typeElement) {
        final List<AnalysedTypeConverter> converters = new ArrayList<>(typeElement.getEnclosedElements().size());

        for (final Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement instanceof ExecutableElement executableElement && TypeConverterPrism.isPresent(executableElement)) {
                // OK, ensure that this is returning the correct type!
                if (APContext.types().isSameType(typeElement.asType(), executableElement.getReturnType()) && executableElement.getModifiers().containsAll(Set.of(Modifier.PUBLIC, Modifier.STATIC))) {
                    converters.add(new AnalysedTypeConverter(executableElement));
                } else {
                    APContext.messager().printWarning("Annotated element is not public static or does not return the enclosed type", executableElement);
                }
            }
        }

        return List.copyOf(converters);
    }

    public ClassName referenceClassName() {
        return this.referenceClassName;
    }

    public String methodName() {
        return this.methodName;
    }

    public List<ParameterSpec> parameters() {
        return this.parameters;
    }

    public ExecutableElement executableElement() {
        return this.executableElement;
    }
}

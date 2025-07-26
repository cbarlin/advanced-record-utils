package io.github.cbarlin.aru.core.types;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public record AnalysedTypeConverter(
        TypeName resultingType,
        ClassName enclosedClass,
        String methodName,
        List<ParameterSpec> parameterSpecs,
        ExecutableElement executableElement
) {
    @Override
    public String toString() {
        return methodName + resultingType + enclosedClass.canonicalName() + StringUtils.join(parameterSpecs);
    }
}

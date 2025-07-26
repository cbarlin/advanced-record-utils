package io.github.cbarlin.aru.core.types;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DEFAULT;
import io.micronaut.sourcegen.javapoet.TypeName;

public record UseInterfaceDefaultClass (
    TypeElement element,
    TypeMirror mirror,
    Class<DEFAULT> clazz,
    TypeName name
) {

}

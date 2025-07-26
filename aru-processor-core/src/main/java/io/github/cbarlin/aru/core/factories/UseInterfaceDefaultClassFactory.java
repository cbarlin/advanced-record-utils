package io.github.cbarlin.aru.core.factories;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DEFAULT;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.UseInterfaceDefaultClass;
import io.github.cbarlin.aru.core.wiring.CoreGlobalScope;
import io.micronaut.sourcegen.javapoet.ClassName;

@Factory
@CoreGlobalScope
public final class UseInterfaceDefaultClassFactory {

    @Bean
    UseInterfaceDefaultClass useInterfaceDefaultClass() {
        final Class<DEFAULT> defClass = DEFAULT.class;
        final ClassName className = ClassName.get(defClass);
        final TypeElement element = APContext.elements().getTypeElement(defClass.getCanonicalName());
        final TypeMirror mirror = element.asType();
        return new UseInterfaceDefaultClass(element, mirror, defClass, className);
    }
}

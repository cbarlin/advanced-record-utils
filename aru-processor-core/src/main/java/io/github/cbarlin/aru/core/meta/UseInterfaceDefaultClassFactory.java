package io.github.cbarlin.aru.core.meta;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DEFAULT;
import io.github.cbarlin.aru.core.APContext;
import io.github.cbarlin.aru.core.types.UseInterfaceDefaultClass;
import io.github.cbarlin.aru.core.wiring.AruGlobal;
import io.micronaut.sourcegen.javapoet.ClassName;

@Factory
@AruGlobal
public final class UseInterfaceDefaultClassFactory {

    @Bean
    @AruGlobal
    UseInterfaceDefaultClass useInterfaceDefaultClass() {
        final Class<DEFAULT> defClass = DEFAULT.class;
        final ClassName className = ClassName.get(defClass);
        final TypeElement element = APContext.elements().getTypeElement(defClass.getCanonicalName());
        final TypeMirror mirror = element.asType();
        return new UseInterfaceDefaultClass(element, mirror, defClass, className);
    }
}

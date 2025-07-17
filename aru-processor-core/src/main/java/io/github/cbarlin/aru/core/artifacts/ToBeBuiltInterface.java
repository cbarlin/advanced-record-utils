package io.github.cbarlin.aru.core.artifacts;

import io.github.cbarlin.aru.core.UtilsProcessingContext;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.TypeSpec;

public final class ToBeBuiltInterface extends ToBeBuilt {

    /**
     * Construct a new interface that is being built
     * 
     * @param className The class name of the artifact we are building
     * @param utilsProcessingContext the processing context
     */
    public ToBeBuiltInterface(ClassName className, UtilsProcessingContext utilsProcessingContext) {
        super(className, TypeSpec.interfaceBuilder(className), utilsProcessingContext);
    }
}

package io.github.cbarlin.aru.core.artifacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.cbarlin.aru.core.UtilsProcessingContext;

import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeSpec;

public final class ToBeBuiltRecord extends ToBeBuilt {

    private final List<ParameterSpec> recordComponents = new ArrayList<>();

    /**
     * Construct a new class that is being built
     * 
     * @param className The class name of the artifact we are building
     * @param utilsProcessingContext the processing context
     */
    public ToBeBuiltRecord(ClassName className, UtilsProcessingContext utilsProcessingContext) {
        super(className, TypeSpec.recordBuilder(className), utilsProcessingContext);
    }

    public ToBeBuiltRecord addParameterSpec(ParameterSpec component) {
        recordComponents.add(component);
        return this;
    }

    @Override
    public TypeSpec finishClass() {
        Collections.sort(recordComponents, (psA, psB) -> psA.name.compareTo(psB.name));
        recordComponents.forEach(classBuilder::addRecordComponent);
        return super.finishClass();
    }

}

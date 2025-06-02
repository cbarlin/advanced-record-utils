package io.github.cbarlin.aru.impl.merger.utils.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuiltCollectionType;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.types.dependencies.fastutils.FastUtilsCollectionComponent;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class FastUtilsMerge extends MergerVisitor {

    public FastUtilsMerge() {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD);
    }

    @Override
    protected boolean innerIsApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 2;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof FastUtilsCollectionComponent acc) {
            final var mutableCollectionType = acc.typeName();

            final ParameterSpec paramA = ParameterSpec.builder(acc.typeName(), "elA", Modifier.FINAL)
                .addAnnotation(NULLABLE)
                .addJavadoc("The preferred input")
                .build();
            
            final ParameterSpec paramB = ParameterSpec.builder(acc.typeName(), "elB", Modifier.FINAL)
                .addAnnotation(NULLABLE)
                .addJavadoc("The non-preferred input")
                .build();

            final MethodSpec.Builder method = mergerStaticClass.createMethod(analysedComponent.name(), claimableOperation);
            method.modifiers.clear();
            method.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(NULLABLE)
                .addParameter(paramA)
                .addParameter(paramB)
                .returns(acc.typeName())
                .addJavadoc("Merger for the field {@code $L}", analysedComponent.name())
                .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", OBJECTS)
                .addStatement("return elB")
                .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", OBJECTS)
                .addStatement("return elA")
                .endControlFlow()
                .addStatement("final $T combined = new $T(elA.size() + elB.size())", mutableCollectionType, mutableCollectionType)
                .addStatement("combined.addAll(elA)")
                .addStatement("combined.addAll(elB)");

            if (BuiltCollectionType.JAVA_IMMUTABLE.name().equals(builderOptionsPrism.builtCollectionType())) {
                logTrace(method, "Returning merged collection - making immutable");
                method.addStatement("return combined.stream().filter($T::nonNull).toList()", OBJECTS);
            } else {
                logTrace(method, "Returning merged collection - not making immutable");
                method.addStatement("return combined");
            }
    
            AnnotationSupplier.addGeneratedAnnotation(method, this);
            return true;

        }
        return false;
    }

}

package io.github.cbarlin.aru.impl.merger.utils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.MERGER_UTILS_CLASS;

import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.inject.RequiresBean;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerHolder;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.types.ComponentTargetingRecord;
import io.github.cbarlin.aru.impl.wiring.MergerPerComponentScope;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;

@Singleton
@MergerPerComponentScope
@RequiresBean({ComponentTargetingRecord.class})
public final class OtherProcessed extends MergerVisitor {

    private final Set<String> processedSpecs;
    private final AnalysedRecord other;

    public OtherProcessed(final MergerHolder mergerHolder, final ComponentTargetingRecord ctr) {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD, mergerHolder);
        this.processedSpecs = mergerHolder.processedMethods();
        this.other = ctr.target();
    }
    
    @Override
    protected int innerSpecificity() {
        return 3;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        final TypeName targetTn = analysedComponent.typeName();
        final String methodName = mergeStaticMethodName(targetTn);
        if (Boolean.TRUE.equals(other.settings().prism().merger())) {
            // OK, the other side has a merger utils I can hook into!
            if (processedSpecs.add(methodName)) {
                final ClassName otherMergerClassName = other.utilsClassChildClass(MERGER_UTILS_CLASS, Claims.MERGER_STATIC_CLASS).className();
                final ParameterSpec paramA = ParameterSpec.builder(targetTn, "elA", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .addJavadoc("The preferred input")
                    .build();
                final ParameterSpec paramB = ParameterSpec.builder(targetTn, "elB", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .addJavadoc("The non-preferred input")
                    .build();

                final MethodSpec.Builder method = mergerStaticClass.createMethod(methodName, claimableOperation);
                method.modifiers.clear();
                method.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .addParameter(paramA)
                    .addParameter(paramB)
                    .returns(targetTn)
                    .addJavadoc("Merger for fields of class {@link $T}", targetTn)
                    .addStatement("return $T.merge(elA, elB)", otherMergerClassName);

                AnnotationSupplier.addGeneratedAnnotation(method, this);
                analysedComponent.parentRecord().addCrossReference(other);
            }
            
            return true;
        }
        return false;
    }
}

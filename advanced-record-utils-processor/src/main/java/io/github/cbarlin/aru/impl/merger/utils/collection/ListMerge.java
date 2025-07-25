package io.github.cbarlin.aru.impl.merger.utils.collection;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.ARRAY_LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.LIST;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.BuiltCollectionType;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.impl.types.AnalysedCollectionComponent;
import io.github.cbarlin.aru.core.types.AnalysedComponent;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public final class ListMerge extends MergerVisitor {

    private final Set<String> processedSpecs = HashSet.newHashSet(5);

    public ListMerge() {
        super(Claims.MERGER_ADD_FIELD_MERGER_METHOD);
    }

    @Override
    protected boolean innerIsApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitComponentImpl(AnalysedComponent analysedComponent) {
        if (analysedComponent instanceof AnalysedCollectionComponent acc && acc.isList()) {
            final var targetTn = analysedComponent.typeName();
            final String methodName = mergeStaticMethodName(targetTn);
            if (processedSpecs.add(methodName)) {
                final var innerType = acc.unNestedPrimaryTypeName();
                final var mutableCollectionType = ARRAY_LIST;
                final var baseType = LIST;

                final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(baseType, innerType);
                final ParameterizedTypeName mutableTypeName = ParameterizedTypeName.get(mutableCollectionType, innerType);


                final ParameterSpec paramA = ParameterSpec.builder(paramTypeName, "elA", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .addJavadoc("The preferred input")
                    .build();
                
                final ParameterSpec paramB = ParameterSpec.builder(paramTypeName, "elB", Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .addJavadoc("The non-preferred input")
                    .build();

                final MethodSpec.Builder method = mergerStaticClass.createMethod(methodName, claimableOperation);
                method.modifiers.clear();
                method.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addAnnotation(NULLABLE)
                    .addParameter(paramA)
                    .addParameter(paramB)
                    .returns(paramTypeName)
                    .addJavadoc("Merger for fields of class {@link $T}", targetTn)
                    .beginControlFlow("if ($T.isNull(elA) || elA.isEmpty())", OBJECTS)
                    .addStatement("return elB")
                    .nextControlFlow("else if ($T.isNull(elB) || elB.isEmpty())", OBJECTS)
                    .addStatement("return elA")
                    .endControlFlow()
                    .addStatement("final $T combined = new $T(elA.size() + elB.size())", mutableTypeName, mutableTypeName)
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
            }
            
            return true;

        }
        return false;
    }

    
}

package io.github.cbarlin.aru.impl.merger.utils;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import org.apache.commons.lang3.StringUtils;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;

@ServiceProvider
public final class MergeMethod extends MergerVisitor {

    public MergeMethod() {
        super(Claims.MERGE_STATIC_MERGE);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        createStaticMethod(analysedRecord);
        if (Boolean.TRUE.equals(mergerOptionsPrism.staticMethodsAddedToUtils())) {
            final ParameterSpec paramA = ParameterSpec.builder(analysedRecord.intendedType(), "preferred", Modifier.FINAL)
                .addAnnotation(NULLABLE)
                .addJavadoc("The preferred element")
                .build();
            final ParameterSpec paramB = ParameterSpec.builder(analysedRecord.intendedType(), "other", Modifier.FINAL)
                .addAnnotation(NULLABLE)
                .addJavadoc("The non-preferred element")
                .build();
            final var method = analysedRecord.utilsClass().createMethod(mergerOptionsPrism.mergerMethodName(), claimableOperation)
                .addModifiers(Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(NULLABLE)
                .addParameter(paramA)
                .addParameter(paramB)
                .addJavadoc("Merge two instances of {@link $T} together", analysedRecord.intendedType())
                .returns(analysedRecord.intendedType())
                .addStatement(
                    "return $T.$L(preferred, other)",
                    mergerStaticClass.className(),
                    mergerOptionsPrism.mergerMethodName()
                );
            AnnotationSupplier.addGeneratedAnnotation(method, this);
                
        }
        return true;
            
    }

    private void createStaticMethod(final AnalysedRecord analysedRecord) {
        final ParameterSpec paramA = ParameterSpec.builder(analysedRecord.intendedType(), "preferred", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The preferred element")
            .build();
        final ParameterSpec paramB = ParameterSpec.builder(analysedRecord.intendedType(), "other", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The non-preferred element")
            .build();
        final MethodSpec.Builder method = mergerStaticClass.createMethod("merge", claimableOperation)
            .addModifiers(Modifier.STATIC)
            .addAnnotation(NULLABLE)
            .addParameter(paramA)
            .addParameter(paramB)
            .addJavadoc("Merge two instances of {@link $T} together", analysedRecord.intendedType())
            .returns(analysedRecord.intendedType())
            .beginControlFlow("if ($T.isNull(other)) ", OBJECTS);
        
        logTrace(method, "Short-circuit of merge - other is null");
        method.addStatement("return preferred")
            .nextControlFlow("else if ($T.isNull(preferred))", OBJECTS);
        logTrace(method, "Short-circuit of merge - preferred is null");
        method.addStatement("return other")
            .endControlFlow();

        final List<String> format = new ArrayList<>();
        final List<Object> args = new ArrayList<>();
        logTrace(method, "Merging two instances together");

        args.add(analysedRecord.builderArtifact().className());
        args.add(builderOptionsPrism.emptyCreationName());

        for (final VariableElement parameter : analysedRecord.intendedConstructor().getParameters()) {
            format.add(".$L($T.$L(preferred.$L(), other.$L()))");
            args.add(parameter.getSimpleName().toString());
            args.add(mergerStaticClass.className());
            args.add(mergeStaticMethodName(TypeName.get(parameter.asType())));
            args.add(parameter.getSimpleName().toString());
            args.add(parameter.getSimpleName().toString());
        }

        args.add(builderOptionsPrism.buildMethodName());
        method.addStatement("return $T.$L()\n" + StringUtils.join(format, "\n") + "\n.$L()", args.toArray());

        AnnotationSupplier.addGeneratedAnnotation(method, this);
    }
}

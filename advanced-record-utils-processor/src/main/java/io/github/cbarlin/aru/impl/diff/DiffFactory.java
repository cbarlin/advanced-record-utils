package io.github.cbarlin.aru.impl.diff;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.annotations.AdvancedRecordUtils.DiffEvaluationMode;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.diff.holders.DiffHolder;
import io.github.cbarlin.aru.impl.diff.holders.DiffInterfaceClass;
import io.github.cbarlin.aru.impl.diff.holders.DiffResultsClass;
import io.github.cbarlin.aru.impl.diff.holders.DiffStaticClass;
import io.github.cbarlin.aru.impl.misc.MatchingInterface;
import io.github.cbarlin.aru.impl.wiring.DiffPerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.github.cbarlin.aru.prism.prison.DiffOptionsPrism;
import io.micronaut.sourcegen.javapoet.ClassName;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.HashSet;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULL_MARKED;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.DIFFER_UTILS_CLASS;

@Factory
@DiffPerRecordScope
public final class DiffFactory {

    private static final String NON_NULL_CHECK = "$T.requireNonNull($L, $S)";
    private static final String JAVADOC_LAZY = "Results are lazily computed on an as-needed basis";
    private static final String JAVADOC_EAGER = "Results are pre-computed and stored in memory";
    private static final String JAVADOC_NEW_LINE = "\n<p>\n";

    private final AnalysedRecord analysedRecord;
    private final DiffOptionsPrism diffOptionsPrism;
    private final ClassName differResultClassName;
    private final MatchingInterface matchingInterface;

    public DiffFactory(final AnalysedRecord analysedRecord, final AdvancedRecordUtilsPrism advancedRecordUtilsPrism, final UtilsClass utilsClass, final MatchingInterface matchingInterface) {
        this.analysedRecord = analysedRecord;
        this.diffOptionsPrism = advancedRecordUtilsPrism.diffOptions();
        final String resultName = diffOptionsPrism.diffResultPrefix() + analysedRecord.typeSimpleName() + diffOptionsPrism.diffResultSuffix();
        differResultClassName = utilsClass.className().nestedClass(resultName);
        this.matchingInterface = matchingInterface;
    }
    
    @Bean
    DiffOptionsPrism diffOptionsPrism() {
        return diffOptionsPrism;
    }

    @Bean
    @BeanTypes(DiffInterfaceClass.class)
    DiffInterfaceClass diffInterfaceClass() {
        final ToBeBuilt differInterface = analysedRecord.utilsClassChildInterface(diffOptionsPrism.differName(), DiffInterfaceClass.claimableOperation());
     
        AnnotationSupplier.addGeneratedAnnotation(differInterface, DiffFactory.class, DiffInterfaceClass.claimableOperation());
        differInterface.builder()
            .addAnnotation(NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.STATIC)
            .addJavadoc("Interface for a record that can compute differences against another instance of the same type")
            .addSuperinterface(matchingInterface.className());

        final MethodSpec.Builder builder = differInterface.createMethod(diffOptionsPrism.differMethodName(), DiffInterfaceClass.claimableOperation())
            .addAnnotation(NON_NULL)
            .addModifiers(Modifier.DEFAULT)
            .addParameter(
                ParameterSpec.builder(differInterface.className(), diffOptionsPrism.comparedToElementName(), Modifier.FINAL)
                    .addAnnotation(NON_NULL)
                    .build()
            )
            .returns(differResultClassName)
            .addStatement("return new $T(this, $L)", differResultClassName, diffOptionsPrism.comparedToElementName())
            .addJavadoc("Generate the diff between this instance ($S) and the provided instance ($S)", diffOptionsPrism.originatingElementName(), diffOptionsPrism.comparedToElementName())
            .addJavadoc(JAVADOC_NEW_LINE);
        
        if (!DiffEvaluationMode.LAZY.name().equals(diffOptionsPrism.evaluationMode())) {
            builder.addJavadoc("Diff is computed as soon as this method is called");
        } else {
            builder.addJavadoc("Diff is computed as requested");
        }
        builder.addJavadoc("\n@return The result of the diff");

        AnnotationSupplier.addGeneratedAnnotation(builder, DiffFactory.class, DiffInterfaceClass.claimableOperation());

        return new DiffInterfaceClass(differInterface);
    }

    @Bean
    @BeanTypes(DiffStaticClass.class)
    DiffStaticClass diffStaticClass() {
        final ToBeBuilt differStaticClass = analysedRecord.utilsClassChildClass(DIFFER_UTILS_CLASS, DiffStaticClass.claimableOperation());
        AnnotationSupplier.addGeneratedAnnotation(differStaticClass, DiffFactory.class, DiffStaticClass.claimableOperation());
        differStaticClass.builder()
            .addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        final MethodSpec.Builder methodBuilder = differStaticClass.createConstructor();
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, DiffFactory.class, DiffStaticClass.claimableOperation());
        methodBuilder.modifiers.clear();
        methodBuilder.addModifiers(Modifier.PRIVATE);
        methodBuilder.addStatement("throw new $T($S)", UNSUPPORTED_OPERATION_EXCEPTION, "This is a utility class and cannot be instantiated");
        return new DiffStaticClass(differStaticClass, new HashSet<>());
    }

    @Bean
    @BeanTypes(DiffResultsClass.class)
    DiffResultsClass resultsClass(final DiffInterfaceClass differInterface) {
        final String resultName = diffOptionsPrism.diffResultPrefix() + analysedRecord.typeSimpleName() + diffOptionsPrism.diffResultSuffix();
        final ToBeBuilt differResult = analysedRecord.utilsClassChildClass(resultName, Claims.DIFFER_RESULT);
        AnnotationSupplier.addGeneratedAnnotation(differResult, DiffFactory.class, Claims.DIFFER_RESULT);
        final MethodSpec.Builder differResultRecordConstructor = differResult.createConstructor();
        final MethodSpec.Builder differResultInterfaceConstructor = differResult.createMethod("<init>", Claims.DIFFER_IFACE);
        final TypeSpec.Builder classSpec = differResult.builder();
        classSpec.addAnnotation(NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addJavadoc("The result of a diff between two instances of {@link $T}", analysedRecord.className())
            .addJavadoc(JAVADOC_NEW_LINE)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        differResultRecordConstructor.addJavadoc("Creates a new diff between two instances of {@link $T}", analysedRecord.className())
            .addJavadoc(JAVADOC_NEW_LINE);
        differResultInterfaceConstructor.addJavadoc("Creates a new diff between two instances of {@link $T}", differInterface.className())
            .addJavadoc(JAVADOC_NEW_LINE);

        if (!DiffEvaluationMode.LAZY.name().equals(diffOptionsPrism.evaluationMode())) {
            classSpec.addJavadoc(JAVADOC_EAGER);
            differResultRecordConstructor.addJavadoc(JAVADOC_EAGER);
            differResultInterfaceConstructor.addJavadoc(JAVADOC_EAGER);
        } else {
            classSpec.addJavadoc(JAVADOC_LAZY);
            differResultRecordConstructor.addJavadoc(JAVADOC_LAZY);
            differResultInterfaceConstructor.addJavadoc(JAVADOC_LAZY);
        }

        differResultRecordConstructor
            .addStatement(
                NON_NULL_CHECK,
                OBJECTS,
                diffOptionsPrism.originatingElementName(),
                "The originating element cannot be null"
            )
            .addStatement(
                NON_NULL_CHECK,
                OBJECTS,
                diffOptionsPrism.comparedToElementName(),
                "The (potentially) changed element cannot be null"
            )
            .addParameter(
                ParameterSpec.builder(analysedRecord.className(), diffOptionsPrism.originatingElementName(), Modifier.FINAL)
                    .addJavadoc("The originating element of the diff")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(analysedRecord.className(), diffOptionsPrism.comparedToElementName(), Modifier.FINAL)
                    .addJavadoc("The (potentially) changed element of the diff")
                    .build()
            );

        differResultInterfaceConstructor
            .addStatement(
                NON_NULL_CHECK,
                OBJECTS,
                diffOptionsPrism.originatingElementName(),
                "The originating element cannot be null"
            )
            .addStatement(
                NON_NULL_CHECK,
                OBJECTS,
                diffOptionsPrism.comparedToElementName(),
                "The (potentially) changed element cannot be null"
            )
            .addParameter(
                ParameterSpec.builder(differInterface.className(), diffOptionsPrism.originatingElementName(), Modifier.FINAL)
                    .addJavadoc("The originating element of the diff")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(differInterface.className(), diffOptionsPrism.comparedToElementName(), Modifier.FINAL)
                    .addJavadoc("The (potentially) changed element of the diff")
                    .build()
            );
        return new DiffResultsClass(differResult, differResultRecordConstructor, differResultInterfaceConstructor);
    }

    @Bean
    DiffHolder diffHolder(final DiffInterfaceClass interfaceClass, final DiffStaticClass staticClass, final DiffResultsClass resultsClass, final BuilderOptionsPrism builderOptionsPrism) {
        return new DiffHolder(
            interfaceClass,
            resultsClass,
            staticClass,
            diffOptionsPrism,
            builderOptionsPrism,
            matchingInterface,
            analysedRecord
        );
    }
}

package io.github.cbarlin.aru.impl.merger;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.UNSUPPORTED_OPERATION_EXCEPTION;
import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.MERGER_UTILS_CLASS;

import java.util.HashSet;

import javax.lang.model.element.Modifier;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.UtilsClass;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.misc.MatchingInterface;
import io.github.cbarlin.aru.impl.wiring.MergerPerRecordScope;
import io.github.cbarlin.aru.prism.prison.AdvancedRecordUtilsPrism;
import io.github.cbarlin.aru.prism.prison.BuilderOptionsPrism;
import io.github.cbarlin.aru.prism.prison.MergerOptionsPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;

@Factory
@MergerPerRecordScope
public final class MergerFactory {

    @Bean
    MergerHolder mergerHolder(
        final AnalysedRecord analysedRecord,
        final AdvancedRecordUtilsPrism arup,
        final UtilsClass utilsClass,
        final MatchingInterface matchingInterface
    ) {
        final BuilderOptionsPrism builderOptionsPrism = arup.builderOptions();
        final MergerOptionsPrism mergerOptionsPrism = arup.mergerOptions();
        final String generatedName = mergerOptionsPrism.mergerName();
        final ToBeBuilt mergerInterface = analysedRecord.utilsClassChildInterface(generatedName, Claims.MERGER_IFACE);
        final ToBeBuilt mergerStaticClass = analysedRecord.utilsClassChildClass(MERGER_UTILS_CLASS, Claims.MERGER_STATIC_CLASS);
        
        AnnotationSupplier.addGeneratedAnnotation(mergerStaticClass, MergerFactory.class, Claims.MERGER_STATIC_CLASS);
        mergerStaticClass.builder()
            .addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        
        final MethodSpec.Builder methodBuilder = mergerStaticClass.createConstructor();
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, MergerFactory.class, Claims.MERGER_STATIC_CLASS);
        methodBuilder.modifiers.clear();
        methodBuilder.addModifiers(Modifier.PRIVATE);
        methodBuilder.addStatement("throw new $T($S)", UNSUPPORTED_OPERATION_EXCEPTION, "This is a utility class and cannot be instantiated");

        AnnotationSupplier.addGeneratedAnnotation(mergerInterface, MergerFactory.class, Claims.MERGER_STATIC_CLASS);
        mergerInterface.builder()
            .addAnnotation(CommonsConstants.Names.NULL_MARKED)
            .addOriginatingElement(analysedRecord.typeElement())
            .addModifiers(Modifier.STATIC)
            .addJavadoc("Interface for a record that can be merged with itself.")
            .addJavadoc("\n<p>\n")
            .addJavadoc("Intended merge process is that, for each field:")
            .addJavadoc("\n<ol>\n")
            .addJavadoc("<li>If both of the two instances have a null value, then the result is null</li>\n")
            .addJavadoc("<li>If one of the two instances has a null value, then take the non-null value</li>\n")
            .addJavadoc("<li>If both are non-null, and the field is itself can be merged, then merge the values using the other merger</li>\n")
            .addJavadoc("<li>If both are non-null, and the field is a collection, then union the collections</li>\n")
            .addJavadoc("<li>Otherwise, keep the value in this instance (instead of the one in the other instance)</li>\n")
            .addJavadoc("</ol>\n")
            .addSuperinterface(matchingInterface.className());

        return new MergerHolder(
            mergerInterface,
            mergerStaticClass,
            builderOptionsPrism,
            mergerOptionsPrism,
            matchingInterface,
            analysedRecord,
            new HashSet<>(40)
        );
    }
}

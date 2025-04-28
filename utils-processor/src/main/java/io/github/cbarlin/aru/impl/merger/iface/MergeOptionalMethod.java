package io.github.cbarlin.aru.impl.merger.iface;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OBJECTS;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import javax.lang.model.element.Modifier;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.ParameterizedTypeName;

@ServiceProvider
public class MergeOptionalMethod extends MergerVisitor {

    public MergeOptionalMethod() {
        super(Claims.MERGE_IFACE_MERGE_OPTIONAL);
    }

    @Override
    protected boolean innerIsApplicable(final AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected int innerSpecificity() {
        return 1;
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        final ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(OPTIONAL, analysedRecord.intendedType());
        final ParameterSpec paramA = ParameterSpec.builder(paramTypeName, "other", Modifier.FINAL)
            .addAnnotation(NON_NULL)
            .addJavadoc("The element to merge into this one, if it is present")
            .build();
        final MethodSpec.Builder method = mergerInterface.createMethod(mergerOptionsPrism.mergerMethodName(), claimableOperation, OPTIONAL)
            .addModifiers(Modifier.DEFAULT)
            .addAnnotation(NON_NULL)
            .addParameter(paramA)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Merge the current instance into the other instance, if it is present")
            .addJavadoc("\n")
            .addJavadoc("@return The result of the merge")
            .addStatement("$T.requireNonNull(other, $S)", OBJECTS, "You cannot supply a null Optional parameter")
            .addStatement("return other.map(oth -> this.$L(oth)).orElse(this.merge(($T) null))", mergerOptionsPrism.mergerMethodName(), analysedRecord.intendedType());

        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
            
    }
}

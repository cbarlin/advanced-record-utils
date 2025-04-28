package io.github.cbarlin.aru.impl.merger.iface;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.NON_NULL;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.NULLABLE;
import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;

import io.avaje.spi.ServiceProvider;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;

@ServiceProvider
public class MergeMethod extends MergerVisitor {

    public MergeMethod() {
        super(Claims.MERGE_IFACE_MERGE);
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
        final ParameterSpec paramA = ParameterSpec.builder(analysedRecord.intendedType(), "other", Modifier.FINAL)
            .addAnnotation(NULLABLE)
            .addJavadoc("The element to merge into this one")
            .build();
        final MethodSpec.Builder method = mergerInterface.createMethod(mergerOptionsPrism.mergerMethodName(), claimableOperation)
            .addModifiers(Modifier.DEFAULT)
            .addAnnotation(NON_NULL)
            .addParameter(paramA)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Merge the current instance into the other instance.")
            .addJavadoc("\n")
            .addJavadoc("@return The result of the merge")
            .addStatement("final var optOther = $T.ofNullable(other)", OPTIONAL);

        final List<String> format = new ArrayList<>();
        final List<Object> args = new ArrayList<>();

        args.add(analysedRecord.builderArtifact().className());
        args.add(builderOptionsPrism.emptyCreationName());

        for (final VariableElement parameter : analysedRecord.intendedConstructor().getParameters()) {
            // Ugly!
            // .aField(_MergerUtils.aField(this.aField(), optOther.map(RootItem::aField).orElse(null)))
            format.add(".$L($T.$L(this.$L(), optOther.map($T::$L).orElse(null)))");
            args.add(parameter.getSimpleName().toString());
            args.add(mergerStaticClass.className());
            args.add(parameter.getSimpleName().toString());
            args.add(parameter.getSimpleName().toString());
            args.add(analysedRecord.intendedType());
            args.add(parameter.getSimpleName().toString());
        }

        args.add(builderOptionsPrism.buildMethodName());
        method.addStatement("return $T.$L()\n" + StringUtils.join(format, "\n") + "\n.$L()", args.toArray());

        AnnotationSupplier.addGeneratedAnnotation(method, this);
        return true;
            
    }
}

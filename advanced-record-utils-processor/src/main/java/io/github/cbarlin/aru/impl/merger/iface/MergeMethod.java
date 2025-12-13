package io.github.cbarlin.aru.impl.merger.iface;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.github.cbarlin.aru.impl.merger.MergerHolder;
import io.github.cbarlin.aru.impl.merger.MergerVisitor;
import io.github.cbarlin.aru.impl.wiring.MergerPerRecordScope;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import io.micronaut.sourcegen.javapoet.ParameterSpec;
import io.micronaut.sourcegen.javapoet.TypeName;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

import static io.github.cbarlin.aru.core.CommonsConstants.Names.OPTIONAL;

@Singleton
@MergerPerRecordScope
public final class MergeMethod extends MergerVisitor {

    public MergeMethod(final MergerHolder mergerHolder) {
        super(Claims.MERGE_IFACE_MERGE, mergerHolder);
    }

    @Override
    protected int innerSpecificity() {
        return 0;
    }

    @Override
    protected boolean visitStartOfClassImpl() {
        final ParameterSpec paramA = ParameterSpec.builder(analysedRecord.intendedType().annotated(CommonsConstants.NULLABLE_ANNOTATION), "other", Modifier.FINAL)
            .addJavadoc("The element to merge into this one")
            .build();
        final MethodSpec.Builder method = mergerInterface.createMethod(mergerOptionsPrism.mergerMethodName(), claimableOperation)
            .addModifiers(Modifier.DEFAULT)
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
            args.add(mergeStaticMethodName(TypeName.get(parameter.asType())));
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

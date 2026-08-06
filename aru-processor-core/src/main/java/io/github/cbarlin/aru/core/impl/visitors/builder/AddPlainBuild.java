package io.github.cbarlin.aru.core.impl.visitors.builder;

import io.avaje.inject.Component;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.core.wiring.CorePerRecordScope;
import io.github.cbarlin.aru.prism.prison.IncludeJFRPrism;
import io.micronaut.sourcegen.javapoet.MethodSpec;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@CorePerRecordScope
public final class AddPlainBuild extends RecordVisitor {
    private static final String CONSTRUCT = "return new $T(\n%s\n)";
    private static final String ASSIGN = "final $T $L = new $T(\n%s\n)";
    private static final String CALL_GETTER = "this.$L()";

    public AddPlainBuild(final AnalysedRecord analysedRecord) {
        super(Claims.BUILDER_BUILD, analysedRecord);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    protected void visitEndOfClassImpl() {
        final String methodName = analysedRecord.settings().prism().builderOptions().buildMethodName();
        final MethodSpec.Builder methodBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder", analysedRecord.intendedType())
            .addModifiers(Modifier.PUBLIC);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        addConstruction(methodBuilder);
    }

    public void addConstruction(final MethodSpec.Builder methodBuilder) {
        logTrace(methodBuilder, "Creating new instance");
        final List<String> constructorArgs = new ArrayList<>();
        final List<Object> formatArgs = new ArrayList<>();
        final boolean addingJfr = shouldCommitJfr();
        if (addingJfr) {
            formatArgs.add(analysedRecord.className());
            formatArgs.add("__builtObj");
        }
        formatArgs.add(analysedRecord.className());
        for ( final VariableElement param : analysedRecord.intendedConstructor().getParameters()) {
            constructorArgs.add(CALL_GETTER);
            formatArgs.add(param.getSimpleName().toString());
        }
        final String command = (addingJfr ? ASSIGN : CONSTRUCT).formatted(StringUtils.join(constructorArgs, ",\n\t"));
        final Object[] args = formatArgs.toArray();
        methodBuilder.addStatement(command, args);
        if (addingJfr) {
            methodBuilder.addStatement("this.__aruJfrEvent.commit()");
            methodBuilder.addStatement("return __builtObj");
        }
    }

    private boolean shouldCommitJfr() {
        if (IncludeJFRPrism.isPresent(analysedRecord.typeElement())) {
            final IncludeJFRPrism prism = Objects.requireNonNull(IncludeJFRPrism.getInstanceOn(analysedRecord.typeElement()));
            return !Boolean.FALSE.equals(prism.builder());
        }
        return false;
    }
}

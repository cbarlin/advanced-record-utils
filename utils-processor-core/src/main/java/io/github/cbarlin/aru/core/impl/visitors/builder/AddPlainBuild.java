package io.github.cbarlin.aru.core.impl.visitors.builder;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import org.apache.commons.lang3.StringUtils;

import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants.Claims;
import io.github.cbarlin.aru.core.CommonsConstants.Names;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;

import io.avaje.spi.ServiceProvider;

@ServiceProvider
public class AddPlainBuild extends RecordVisitor {
    private static final String CONSTRUCT = "return new $T(\n%s\n)";
    private static final String CALL_GETTER = "this.$L()";

    public AddPlainBuild() {
        super(Claims.BUILDER_BUILD);
    }

    @Override
    public int specificity() {
        return 0;
    }

    @Override
    public boolean isApplicable(AnalysedRecord analysedRecord) {
        return true;
    }

    @Override
    protected void visitEndOfClassImpl(AnalysedRecord analysedRecord) {
        final String methodName = analysedRecord.settings().prism().builderOptions().buildMethodName();
        final var methodBuilder = analysedRecord.builderArtifact()
            .createMethod(methodName, claimableOperation)
            .returns(analysedRecord.intendedType())
            .addJavadoc("Creates a new instance of {@link $T} from the fields set on this builder", analysedRecord.intendedType())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Names.NON_NULL);
        AnnotationSupplier.addGeneratedAnnotation(methodBuilder, this);
        logTrace(methodBuilder, "Creating new instance");
        final List<String> constructorArgs = new ArrayList<>();
        final List<Object> formatArgs = new ArrayList<>();
        formatArgs.add(analysedRecord.className());
        for ( final VariableElement param : analysedRecord.intendedConstructor().getParameters()) {
            constructorArgs.add(CALL_GETTER);
            formatArgs.add(param.getSimpleName().toString());
        }
        final String command = CONSTRUCT.formatted(StringUtils.join(constructorArgs, ",\n\t"));
        final Object[] args = formatArgs.toArray();
        methodBuilder.addStatement(command, args);
    }
}

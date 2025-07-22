package io.github.cbarlin.aru.impl.misc;

import static io.github.cbarlin.aru.impl.Constants.InternalReferenceNames.INTERNAL_MATCHING_IFACE_NAME;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import io.avaje.spi.ServiceProvider;
import io.github.cbarlin.aru.core.AnnotationSupplier;
import io.github.cbarlin.aru.core.CommonsConstants;
import io.github.cbarlin.aru.core.artifacts.ToBeBuilt;
import io.github.cbarlin.aru.core.artifacts.ToBeBuiltInterface;
import io.github.cbarlin.aru.core.types.AnalysedRecord;
import io.github.cbarlin.aru.core.visitors.RecordVisitor;
import io.github.cbarlin.aru.impl.Constants.Claims;
import io.micronaut.sourcegen.javapoet.TypeSpec;

@ServiceProvider
public final class AllInterfaceGenerator extends RecordVisitor  {

    private static final String GENERATED_NAME = "All";

    public AllInterfaceGenerator() {
        super(Claims.ALL_IFACE);
    }

    // Since this creates the "all" interface, it should be last
    @Override
    public int specificity() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isApplicable(final AnalysedRecord analysedRecord) {
        return analysedRecord.settings().prism().createAllInterface();
    }

    @Override
    protected boolean visitStartOfClassImpl(final AnalysedRecord analysedRecord) {
        final ToBeBuilt builder = analysedRecord.utilsClassChildInterface(GENERATED_NAME, claimableOperation);
        builder.builder().addModifiers(Modifier.PUBLIC);
        final ToBeBuilt matchingIface = analysedRecord.utilsClassChildInterface(INTERNAL_MATCHING_IFACE_NAME, Claims.INTERNAL_MATCHING_IFACE);
        AnnotationSupplier.addGeneratedAnnotation(builder, this);
        final TypeSpec.Builder allTypeSpecBuilder = builder.builder();
        allTypeSpecBuilder.addAnnotation(CommonsConstants.Names.NULL_MARKED);

        // OK, work out if the record has implemented the All interface. If so, we can seal everything!
        final String utilsClassName = analysedRecord.utilsClass().className().simpleName();
        final boolean isAllImplemented = analysedRecord.typeElement().getInterfaces().stream()
            .map(TypeMirror::toString)
            .anyMatch(tm -> tm.contains(GENERATED_NAME) && tm.contains(utilsClassName));
        
        if (isAllImplemented) {
            allTypeSpecBuilder.addModifiers(Modifier.SEALED)
                .addPermittedSubclass(analysedRecord.className());
        }

        final List<ToBeBuiltInterface> otherIfaces = new ArrayList<>();

        analysedRecord.utilsClass().visitChildArtifacts(artifact -> {
            if (artifact != builder && artifact instanceof final ToBeBuiltInterface tbbi && matchingIface != tbbi) {
                allTypeSpecBuilder.addSuperinterface(tbbi.className());
                otherIfaces.add(tbbi);
                if (isAllImplemented) {
                    tbbi.builder().addModifiers(Modifier.SEALED)
                        .addPermittedSubclass(builder.className());
                }
            }
        });

        if (isAllImplemented) {
            final TypeSpec.Builder matchingIfaceBuilder = matchingIface.builder();
            matchingIfaceBuilder.addModifiers(Modifier.SEALED);
            otherIfaces.stream()
                .map(ToBeBuiltInterface::className)
                .forEach(matchingIfaceBuilder::addPermittedSubclass);
        }

        return true;
    }
}

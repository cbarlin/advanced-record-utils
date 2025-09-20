package io.github.cbarlin.aru.core.mirrorhandlers;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import java.util.List;

public sealed interface StackingAnnotationMirror
extends AnnotationMirror, AnnotationValue
permits SourceTrackingAnnotationMirror, MergedMirror
{
    public List<SourceTrackingAnnotationMirror> trackingMirrors();
}

package io.github.cbarlin.aru.core.wiring;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.avaje.inject.Profile;

@Inherited
@Retention(CLASS)
@Profile(all = {"reset-per-component"})
@Target({TYPE, METHOD, ANNOTATION_TYPE})
public @interface ResetPerComponent {

}

package io.github.cbarlin.aru.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker that an item was generated by Advanced Record Utils
 * <p>
 * Mostly for internal use, but also so that things like SonarQube don't measure metrics of generated code
 */
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.CLASS)
public @interface Generated {

    String[] value();

    String comments() default "";
}

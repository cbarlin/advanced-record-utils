package io.github.cbarlin.aru.core.wiring;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.avaje.inject.Profile;

// @Scope
// @InjectModule(
//     name = "AruGlobal"
// )
@Inherited
@Retention(SOURCE)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
@Profile(all = {"aru-global"})
public @interface AruGlobal {

}

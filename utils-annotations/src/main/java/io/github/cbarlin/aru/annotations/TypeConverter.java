package io.github.cbarlin.aru.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A type converter that's accessible to a builder. Must be {@code public static}.
 * When generating builders if targeting the return/built type is
 *  the same as the one being given a setter method, another setter method will be generated
 *  that takes in the type arguments as parameters
 * <p>
 * The difference between {@code TypeConverter} and {@link TypeAlias} is
 *  that these types will not be used for serialisation, whereas the TypeAlias will.
 * <p>
 * An example of how this can be used:
 * <p>
 * <pre>{@code
 *
 *   @TypeConverter
 *   public static MyRecord fromSomethingElse(SomeOtherClass someOtherClass) {
 *      return new MyRecord(...);
 *   }
 *
 * }</pre>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD })
public @interface TypeConverter {}

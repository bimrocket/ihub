package org.bimrocket.ihub.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author realor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigProperty
{
  String name() default "";

  String description() default "";

  boolean required() default true;

  boolean secret() default false;

  String contentType() default "";
}

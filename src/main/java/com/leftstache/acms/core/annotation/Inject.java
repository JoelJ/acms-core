package com.leftstache.acms.core.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Joel Johnson
 */
@Documented
@Retention (RUNTIME)
@Target({METHOD})
public @interface Inject {
	String value() default "";
}

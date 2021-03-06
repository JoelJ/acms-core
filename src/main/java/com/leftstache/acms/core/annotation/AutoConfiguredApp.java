package com.leftstache.acms.core.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Joel Johnson
 */
@Documented
@Retention (RUNTIME)
@Target(TYPE)
public @interface AutoConfiguredApp {
	String[] packages() default {};
}

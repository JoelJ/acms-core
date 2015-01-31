package com.leftstache.acms.core.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Joel Johnson
 */
@Documented
@Retention (RUNTIME)
@Target({TYPE})
public @interface Injectable {
	String value() default "";
}

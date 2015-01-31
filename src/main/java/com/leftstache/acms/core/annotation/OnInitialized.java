package com.leftstache.acms.core.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated method will be invoked when all values have been injected
 *
 * @author Joel Johnson
 */
@Documented
@Retention (RUNTIME)
@Target({METHOD})
public @interface OnInitialized {
}

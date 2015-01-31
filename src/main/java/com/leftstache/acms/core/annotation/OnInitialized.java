package com.leftstache.acms.core.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated method will be invoked when all values have been injected throughout the app.
 * There is no guaranteed order in which these methods will be invoked,
 * so it cannot be assumed that any OnInitialized method has been called on any other bean.
 *
 * @author Joel Johnson
 */
@Documented
@Retention (RUNTIME)
@Target({METHOD})
public @interface OnInitialized {
}

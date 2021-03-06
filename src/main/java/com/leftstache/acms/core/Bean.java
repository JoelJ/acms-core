package com.leftstache.acms.core;

import com.leftstache.acms.core.annotation.*;
import com.leftstache.acms.core.exception.*;
import com.leftstache.acms.core.utils.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * @author Joel Johnson
 */
public class Bean<T> {
	private final String name;
	private final Class<T> type;
	private final T instance;
	private final Collection<Field> injectedFields;
	private final Collection<Method> onInitializedMethods;

	public Bean(String name, Class<T> type, T instance) {
		this.name = name;
		this.type = type;
		this.instance = instance;

		injectedFields = ReflectionUtils.findDeclaredFieldsRecursively(instance.getClass(), f -> f.getAnnotation(Inject.class) != null);
		onInitializedMethods = ReflectionUtils.findDeclaredMethodsRecursively(instance.getClass(), m -> m.getAnnotation(OnInitialized.class) != null);
	}

	public String getName() {
		return name;
	}

	public Class<T> getType() {
		return type;
	}

	public T getInstance() {
		return instance;
	}

	public Collection<Field> getInjectedFields() {
		return injectedFields;
	}

	public Collection<Method> getOnInitializedMethods() {
		return onInitializedMethods;
	}

	public void fireOnInitializedMethods() {
		for (Method onInitializedMethod : onInitializedMethods) {
			try {
				onInitializedMethod.invoke(instance);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new ReflectionException("Could not invoke OnInitialized method " + onInitializedMethod.getName() + " for bean instance " + instance, e);
			}
		}
	}

	/**
	 * @returns true if the bean instance is annotated with the given annotation type.
	 */
	public boolean isAnnotatedWith(Class<? extends Annotation> annotationClass) {
		return this.getInstance().getClass().getAnnotation(annotationClass) != null;
	}

	public <ANNOTATION extends Annotation> ANNOTATION getTypeAnnotation(Class<ANNOTATION> annotationClass) {
		return this.getInstance().getClass().getAnnotation(annotationClass);
	}

	public Collection<Method> getMethods(Predicate<Method> filter) {
		Class<?> type = getInstance().getClass();
		return ReflectionUtils.findDeclaredMethodsRecursively(type, filter);
	}
}

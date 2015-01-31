package com.leftstache.acms.core;

import com.leftstache.acms.core.annotation.*;
import com.leftstache.acms.core.utils.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Joel Johnson
 */
public class Bean<T> {
	private final String name;
	private final Class<T> type;
	private final T instance;
	private final Collection<Field> injectedFields;

	public Bean(String name, Class<T> type, T instance) {
		this.name = name;
		this.type = type;
		this.instance = instance;

		injectedFields = ReflectionUtils.findDeclaredFieldsRecursively(instance.getClass(), f -> f.getAnnotation(Inject.class) != null);
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
}

package com.leftstache.acms.core;

import com.leftstache.acms.core.annotation.*;
import com.leftstache.acms.core.exception.*;
import com.leftstache.acms.core.utils.ReflectionUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Joel Johnson
 */
public class AcmsApplication<T> {
	private final Class<T> applicationClass;
	private final T application;
	private final BeanIndexer beanIndexer;

	public AcmsApplication(Class<T> applicationClass, BeanIndexer beanIndexer) {
		this.applicationClass = applicationClass;
		try {
			this.application = applicationClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new AcmsException(e);
		}
		this.beanIndexer = beanIndexer;
	}

	public static <T> AcmsApplication<T> run(Class<T> applicationClass) {
		if(applicationClass == null) {
			throw new NullPointerException("applicationClass");
		}

		if(applicationClass.getAnnotation(AutoConfiguredApp.class) == null) {
			throw new AcmsException("Unabled to initialize app not annotated with AutoConfiguredApp");
		}

		AcmsApplication acmsApplication;
		acmsApplication = new AcmsApplication(applicationClass, new BeanIndexerImpl());
		acmsApplication.start();
		return acmsApplication;
	}

	void start() {
		Collection<Method> declaredMethodsRecursively = ReflectionUtils.findDeclaredMethodsRecursively(applicationClass, m -> m.getAnnotation(Inject.class) != null);

		// Collect all the no arg beans first, since they have no dependencies and other beans might depend on it
		declaredMethodsRecursively.stream().filter(m -> m.getParameterCount() == 0).forEach(this::indexNoArgMethod);

		// Now collect beans that have depenedencies
		declaredMethodsRecursively.stream().filter(m -> m.getParameterCount() != 0).forEach(this::indexMethod);
	}

	private void indexNoArgMethod(Method method) {
		assert method.getParameterCount() == 0;
		assert method.getAnnotation(Inject.class) != null;

		Object bean;
		try {
			bean = method.invoke(application);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new ReflectionException("Unable to create bean from method: " + method.getName(), e);
		}

		Class<?> type = method.getReturnType();
		if(type == null || type == Void.class) {
			throw new ReflectionException("Bean methods should have a return value");
		}

		Inject injectAnnotation = method.getAnnotation(Inject.class);
		String beanName = injectAnnotation.value();
		if(beanName == null || beanName.isEmpty()) {
			beanName = method.getName();
		}

		beanIndexer.index(type, bean, beanName);
	}

	private void indexMethod(Method method) {
		assert method.getParameterCount() != 0;
	}

	public Class<?> getApplicationClass() {
		return applicationClass;
	}

	public T getApplication() {
		return application;
	}

	public BeanIndexer getBeanIndexer() {
		return beanIndexer;
	}
}

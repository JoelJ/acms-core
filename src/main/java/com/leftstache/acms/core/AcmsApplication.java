package com.leftstache.acms.core;

import com.leftstache.acms.core.annotation.*;
import com.leftstache.acms.core.exception.*;
import com.leftstache.acms.core.utils.ReflectionUtils;

import java.lang.reflect.*;
import java.util.*;

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
		try {
			indexMethods(declaredMethodsRecursively);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new ReflectionException("Exception while creating beans from methods", e);
		}
	}

	private void indexMethods(Collection<Method> methods) throws InvocationTargetException, IllegalAccessException {
		List<Method> deferredMethods = new ArrayList<>();
		for (Method method : methods) {
			if(!indexMethod(method)) {
				deferredMethods.add(method);
			}
		}

		int lastCount = deferredMethods.size();
		while(deferredMethods.size() > 0) {
			List<Method> previouslyDeferred = deferredMethods;
			deferredMethods = new ArrayList<>();
			for (Method deferredMethod : previouslyDeferred) {
				if(!indexMethod(deferredMethod)) {
					deferredMethods.add(deferredMethod);
				}
			}

			if(deferredMethods.size() == lastCount) {
				StringBuilder sb = new StringBuilder();
				sb.append(deferredMethods.get(0).getName());

				for(int i = 1; i < deferredMethods.size(); i++) {
					sb.append(", ").append(deferredMethods.get(i).getName());
				}

				String message = sb.toString();
				throw new AcmsException("Unable to resolve dependencies for bean methods: " + message);
			}
		}
	}

	private boolean indexMethod(Method method) throws InvocationTargetException, IllegalAccessException {
		Parameter[] parametersDefs = method.getParameters();

		Object[] parameterValues;
		if(parametersDefs.length == 0) {
			parameterValues = new Object[0];
		} else {
			parameterValues = new Object[parametersDefs.length];
			for (int i = 0; i < parametersDefs.length; i++) {
				Parameter parameterDef = parametersDefs[i];
				String parameterName = parameterDef.getName();
				Object bean = beanIndexer.getBeanByName(parameterName);
				if (bean == null) {
					return false; // Uh-oh, we don't have that value yet! Defer until later. Hopefully we'll create it soon!
				}
				parameterValues[i] = bean;
			}
		}

		Object bean = method.invoke(application, parameterValues);

		String beanName = method.getName();
		Inject inject = method.getAnnotation(Inject.class);
		if(inject != null && !inject.value().isEmpty()) {
			beanName = inject.value();
		}
		beanIndexer.index(method.getReturnType(), bean, beanName);

		return true;
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

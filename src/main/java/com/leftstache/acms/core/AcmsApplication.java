package com.leftstache.acms.core;

import com.leftstache.acms.core.annotation.*;
import com.leftstache.acms.core.exception.*;
import com.leftstache.acms.core.utils.ReflectionUtils;
import javafx.scene.effect.*;
import org.reflections.*;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author Joel Johnson
 */
public class AcmsApplication<T> {
	private final Class<T> applicationClass;
	private final Collection<String> externalPackages;
	private final T application;
	private final BeanIndexer beanIndexer;

	public AcmsApplication(Class<T> applicationClass, BeanIndexer beanIndexer, Collection<String> externalPackages) {
		this.applicationClass = applicationClass;
		this.externalPackages = externalPackages;
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

		Collection<String> externalPackages = ReflectionUtils.findInjectedPackages();

		AcmsApplication acmsApplication;
		acmsApplication = new AcmsApplication(applicationClass, new BeanIndexerImpl(), externalPackages);
		acmsApplication.start();
		return acmsApplication;
	}

	void start() {
		loadExternallyInjectedBeans();

		Collection<Method> declaredMethodsRecursively = ReflectionUtils.findDeclaredMethodsRecursively(applicationClass, m -> m.getAnnotation(Inject.class) != null);

		// Index all the method-declared beans
		try {
			indexMethods(declaredMethodsRecursively, application);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new ReflectionException("Exception while creating beans from methods", e);
		}

		// Now find all the injected values and make sure their injected values are also injected
		indexNestedInjected();

		// Done injecting! Fire all the OnInitialized methods.
		fireOnInitialized();
	}

	private void loadExternallyInjectedBeans() {
		for (String externalPackage : externalPackages) {
			Reflections reflections = new Reflections(externalPackage);
			Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Inject.class);

			for (Class<?> annotatedType : typesAnnotatedWith) {
				indexType(annotatedType);
			}
		}
	}

	private void indexType(Class<?> type) {
		assert type.getAnnotation(Inject.class) != null;

		Inject annotation = type.getAnnotation(Inject.class);
		String name;
		if(annotation.value().isEmpty()) {
			name = Introspector.decapitalize(type.getSimpleName());
		} else {
			name = annotation.value();
		}

		Object instance;
		try {
			instance = type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ReflectionException("Unable to create instance of external bean " + type, e);
		}

		beanIndexer.index(type, instance, name);

		Collection<Method> declaredMethodsRecursively = ReflectionUtils.findDeclaredMethodsRecursively(instance.getClass(), m -> m.getAnnotation(Inject.class) != null);
		try {
			indexMethods(declaredMethodsRecursively, instance);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new ReflectionException("Unable to index bean methods for external bean " + type, e);
		}
	}

	private void fireOnInitialized() {
		Collection<Bean<?>> allBeans = beanIndexer.getAllBeans();
		allBeans.forEach(Bean::fireOnInitializedMethods);
	}

	private void indexNestedInjected() {
		Collection<Bean<?>> allBeans = beanIndexer.getAllBeans();

		for (Bean bean : allBeans) {
			Collection<Field> fields = bean.getInjectedFields();
			fields.forEach(i -> injectField(bean, i));
		}
	}

	private void injectField(Bean bean, Field field) {
		Class<?> type = field.getType();
		String name = field.getName();

		Inject annotation = field.getAnnotation(Inject.class);
		if(!annotation.value().isEmpty()) {
			name = annotation.value();
		}

		Bean<?> beanToInject = beanIndexer.getBean(type, name);
		if(beanToInject != null) {
			try {
				field.set(bean.getInstance(), beanToInject.getInstance());
			} catch (IllegalAccessException e) {
				throw new ReflectionException("Exception while injecting bean with depenencies. Injecting " + bean.getClass().getName() + " with " + beanToInject, e);
			}
		}
	}

	private void indexMethods(Collection<Method> methods, Object instance) throws InvocationTargetException, IllegalAccessException {
		List<Method> deferredMethods = new ArrayList<>();
		for (Method method : methods) {
			if(!indexMethod(method, instance)) {
				deferredMethods.add(method);
			}
		}

		int lastCount = deferredMethods.size();
		while(deferredMethods.size() > 0) {
			List<Method> previouslyDeferred = deferredMethods;
			deferredMethods = new ArrayList<>();
			for (Method deferredMethod : previouslyDeferred) {
				if(!indexMethod(deferredMethod, instance)) {
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

	private boolean indexMethod(Method method, Object instance) throws InvocationTargetException, IllegalAccessException {
		Parameter[] parametersDefs = method.getParameters();

		Object[] parameterValues;
		if(parametersDefs.length == 0) {
			parameterValues = new Object[0];
		} else {
			parameterValues = new Object[parametersDefs.length];
			for (int i = 0; i < parametersDefs.length; i++) {
				Parameter parameterDef = parametersDefs[i];
				String parameterName = parameterDef.getName();
				Bean bean = beanIndexer.getBean(parameterDef.getType(), parameterName);
				if (bean == null) {
					return false; // Uh-oh, we don't have that value yet! Defer until later. Hopefully we'll create it soon!
				}
				parameterValues[i] = bean.getInstance();
			}
		}

		Object bean = method.invoke(instance, parameterValues);

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

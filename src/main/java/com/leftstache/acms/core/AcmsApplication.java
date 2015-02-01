package com.leftstache.acms.core;

import com.google.common.collect.*;
import com.leftstache.acms.core.annotation.*;
import com.leftstache.acms.core.exception.*;
import com.leftstache.acms.core.utils.ReflectionUtils;
import org.reflections.*;

import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * @author Joel Johnson
 */
public class AcmsApplication<T> implements Closeable, AutoCloseable {
	private final Class<T> applicationClass;
	private final Collection<String> externalPackages;
	private final T application;
	private final BeanIndexer beanIndexer;
	private List<BeanListener> beanListeners;

	private volatile boolean started = false;
	private final Object $lock$ = new Object();

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

		AutoConfiguredApp autoConfiguredAppAnnotation = applicationClass.getAnnotation(AutoConfiguredApp.class);
		if(autoConfiguredAppAnnotation == null) {
			throw new AcmsException("Unable to initialize app not annotated with AutoConfiguredApp");
		}

		String[] packages;
		if(autoConfiguredAppAnnotation.packages().length <= 0) {
			packages = new String[]{applicationClass.getPackage().getName()};
		} else {
			packages = autoConfiguredAppAnnotation.packages();
		}


		Collection<String> externalPackages = ReflectionUtils.findInjectedPackages();
		externalPackages = ImmutableSet.<String>builder()
			.addAll(externalPackages)
			.add(packages)
			.build();

		AcmsApplication acmsApplication;
		acmsApplication = new AcmsApplication(applicationClass, new BeanIndexerImpl(), externalPackages);
		acmsApplication.initialize();
		return acmsApplication;
	}

	/**
	 * Starts the app. This method returns after {@link #close()} is called.
	 * Does nothing if start was already called.
	 */
	public void start() {
		if(!started) {
			synchronized ($lock$) {
				if(!started) {
					started = true;

					fireStartApplicationEvent();

					try {
						Runtime.getRuntime().addShutdownHook(new Thread(this::close));
						$lock$.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}

					fireCloseApplicationEvent();
				}
			}
		}
	}

	private void fireCloseApplicationEvent() {
		Collection<Bean<?>> allBeans = beanIndexer.getAllBeans();
		for (Bean<?> bean : allBeans) {
			Object instance = bean.getInstance();
			if(instance instanceof ApplicationListener) {
				((ApplicationListener) instance).applicationClose(this);
			}
		}
	}

	private void fireStartApplicationEvent() {
		Collection<Bean<?>> allBeans = beanIndexer.getAllBeans();
		for (Bean<?> bean : allBeans) {
			Object instance = bean.getInstance();
			if(instance instanceof ApplicationListener) {
				((ApplicationListener) instance).applicationStart(this);
			}
		}
	}

	/**
	 * Must be called after {@link #start()}. Will allow the {@link #start()} method to return.
	 * Does nothing if {@link #start()} was never called.
	 */
	public void close() {
		if(started) {
			synchronized ($lock$) {
				if(started) {
					$lock$.notifyAll();
					started = false;
				}
			}
		}
	}

	void initialize() {
		loadExternalObjects();

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
		Collection<Bean<?>> allBeans = beanIndexer.getAllBeans();
		allBeans.forEach(Bean::fireOnInitializedMethods);

		// Notify all listeners that the various beans are done.
		allBeans.forEach(this::fireBeanListenersPostInjected);
	}

	private void loadExternalObjects() {
		for (String externalPackage : externalPackages) {
			Reflections reflections = new Reflections(externalPackage);
			loadExternallyInjectedBeans(reflections);
		}

		this.beanListeners = beanIndexer.getAllBeans().stream().filter(bean -> bean.getInstance() instanceof BeanListener).map(bean -> (BeanListener) bean.getInstance()).collect(Collectors.toList());
	}

	private void loadExternallyInjectedBeans(Reflections reflections) {
		Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Inject.class);
		typesAnnotatedWith.forEach(this::indexType);
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

		fireBeanListenersPreInitialize(type, name);

		Object instance;
		try {
			instance = type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ReflectionException("Unable to create instance of external bean " + type, e);
		}

		instance = fireBeanListenersPostInitialize(type, instance, name);

		beanIndexer.index(type, instance, name);

		Collection<Method> declaredMethodsRecursively = ReflectionUtils.findDeclaredMethodsRecursively(instance.getClass(), m -> m.getAnnotation(Inject.class) != null);
		try {
			indexMethods(declaredMethodsRecursively, instance);
		} catch (InvocationTargetException | IllegalAccessException e) {
			throw new ReflectionException("Unable to index bean methods for external bean " + type, e);
		}
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
				if(!parameterDef.isNamePresent()) {
					throw new AcmsException("The application needs to be compiled with the compiler arguement '-parameters'. Refer to acms-core documentation for more information.");
				}

				String parameterName = parameterDef.getName();
				Bean bean = beanIndexer.getBean(parameterDef.getType(), parameterName);
				if (bean == null) {
					return false; // Uh-oh, we don't have that value yet! Defer until later. Hopefully we'll create it soon!
				}
				parameterValues[i] = bean.getInstance();
			}
		}

		String beanName = method.getName();
		Inject inject = method.getAnnotation(Inject.class);
		if(inject != null && !inject.value().isEmpty()) {
			beanName = inject.value();
		}

		fireBeanListenersPreInitialize(method.getReturnType(), beanName);

		Object bean = method.invoke(instance, parameterValues);

		fireBeanListenersPostInitialize(method.getReturnType(), bean, beanName);

		beanIndexer.index(method.getReturnType(), bean, beanName);

		return true;
	}


	private void fireBeanListenersPreInitialize(Class<?> type, String name) {
		if(beanListeners != null) {
			for (BeanListener beanListener : beanListeners) {
				beanListener.preInitialize(type, name);
			}
		}
	}

	private Object fireBeanListenersPostInitialize(Class<?> type, Object instance, String name) {
		if(beanListeners != null) {
			for (BeanListener beanListener : beanListeners) {
				instance = beanListener.postInitialize(type, instance, name);
			}
		}
		return instance;
	}

	private void fireBeanListenersPostInjected(Bean<?> bean) {
		if(beanListeners != null) {
			for (BeanListener beanListener : beanListeners) {
				beanListener.postInjected(bean);
			}
		}
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

	public boolean isStarted() {
		return started;
	}
}

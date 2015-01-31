package com.leftstache.acms.core;

import com.google.common.collect.*;
import com.leftstache.acms.core.annotation.*;
import com.leftstache.acms.core.exception.*;
import org.reflections.*;

import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Joel Johnson
 */
public class AcmsApplication {
	private final Class<?> applicationClass;
	private final Set<Class<?>> injectedAppTypes;
	private final Set<Class<? extends AutoConfigureListener>> autoconfiguredTypes;
	private final Injector injector;

	public AcmsApplication(Class<?> applicationClass, Injector injector, Collection<String> autoconfigurePackages) {
		this.applicationClass = applicationClass;
		this.autoconfiguredTypes = findAutoconfiguredTypes(autoconfigurePackages);
		this.injectedAppTypes = findInjectedAppTypes(applicationClass);
		this.injector = injector;
	}

	public static AcmsApplication run(Class<?> applicationClass, String... args) {
		if(applicationClass == null) {
			throw new NullPointerException("applicationClass");
		}

		AcmsApplication acmsApplication;
		try {
			Collection<String> autoconfiguredPackages = findAutoconfiguredPackages();
			acmsApplication = new AcmsApplication(applicationClass, new InjectorImpl(), autoconfiguredPackages);
			acmsApplication.start();
		} catch (IllegalAccessException | InstantiationException | IOException e) {
			throw new AcmsException(e);
		}
		return acmsApplication;
	}

	private void start() throws IllegalAccessException, InstantiationException {
		List<AutoConfigureListener> autoConfigureListeners = new ArrayList<>();
		for (Class<? extends AutoConfigureListener> autoconfiguredType : autoconfiguredTypes) {
			autoConfigureListeners.add(autoconfiguredType.newInstance());
		}

		for (Class<?> injectedAppType : injectedAppTypes) {
			autoConfigureListeners.forEach(i -> i.autoConfiguredClassFound(injectedAppType));

			Injectable annotation = injectedAppType.getAnnotation(Injectable.class);

			Object instance = injectedAppType.newInstance();

			String beanName;
			String annotationName = annotation.value().trim();
			if(annotationName.isEmpty()) {
				beanName = Introspector.decapitalize(injectedAppType.getSimpleName());
			} else {
				beanName = annotationName;
			}

			for (AutoConfigureListener autoConfigureListener : autoConfigureListeners) {
				instance = autoConfigureListener.autoConfiguredClassInitialized(instance, beanName);
			}

			injector.index(instance, beanName);
		}
	}

	private static Collection<String> findAutoconfiguredPackages() throws IOException {
		Set<String> autoconfiguredPackages = new HashSet<>();

		Enumeration<URL> resources;
		try {
			resources = AcmsApplication.class.getClassLoader().getResources("injected.acms");
		} catch (IOException e) {
			throw new AcmsException("Unable to load injected.acms files from class loader", e);
		}

		while(resources.hasMoreElements()) {
			URL url = resources.nextElement();
			try (InputStream inputStream = url.openStream()) {
				Scanner scanner = new Scanner(inputStream);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					autoconfiguredPackages.add(line);
				}
			}
		}

		return ImmutableSet.copyOf(autoconfiguredPackages);
	}

	private static Set<Class<? extends AutoConfigureListener>> findAutoconfiguredTypes(Collection<String> autoconfiguredPackages) {
		Set<Class<? extends AutoConfigureListener>> autoconfigureListeners = new HashSet<>();

		autoconfiguredPackages.stream().filter(packageToLoad -> !packageToLoad.isEmpty()).forEach(packageToLoad -> {
			Reflections reflections = new Reflections(packageToLoad);
			Set<Class<? extends AutoConfigureListener>> listeners = reflections.getSubTypesOf(AutoConfigureListener.class);
			autoconfigureListeners.addAll(listeners);
		});

		return ImmutableSet.copyOf(autoconfigureListeners);
	}

	private static Set<Class<?>> findInjectedAppTypes(Class<?> applicationClass) {
		AutoConfiguredApp annotation = applicationClass.getAnnotation(AutoConfiguredApp.class);
		if(annotation == null) {
			throw new AcmsException("Can only run classes annotated with AutoConfiguredApp");
		}
		String[] packages = annotation.packages();
		if(packages.length <= 0) {
			packages = new String[] { applicationClass.getPackage().getName() };
		}

		Set<Class<?>> annotatedTypes = new HashSet<>();
		for (String pkg : packages) {
			Reflections reflections = new Reflections(pkg);
			Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Injectable.class);
			annotatedTypes.addAll(typesAnnotatedWith);
		}

		return ImmutableSet.copyOf(annotatedTypes);
	}

	public Class<?> getApplicationClass() {
		return applicationClass;
	}

	public Injector getInjector() {
		return injector;
	}

	public Set<Class<?>> getInjectedAppTypes() {
		return injectedAppTypes;
	}

	public Set<Class<? extends AutoConfigureListener>> getAutoconfiguredTypes() {
		return autoconfiguredTypes;
	}
}

package com.leftstache.acms.core.utils;

import com.google.common.collect.*;
import com.leftstache.acms.core.exception.*;
import jdk.nashorn.internal.ir.annotations.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.function.*;

/**
 * @author Joel Johnson
 */
public class ReflectionUtils {
	public static Collection<Method> findDeclaredMethodsRecursively(Class<?> startingClass, Predicate<Method> filter) {
		List<Method> allDeclaredMethods = new ArrayList<>();
		Class<?> nextClass = startingClass;
		while(nextClass != null && nextClass != Object.class) {
			Method[] declaredMethods = nextClass.getDeclaredMethods();

			for (Method declaredMethod : declaredMethods) {
				if(filter.test(declaredMethod)) {
					declaredMethod.setAccessible(true);
					allDeclaredMethods.add(declaredMethod);
				}
			}

			nextClass = nextClass.getSuperclass();
		}

		return ImmutableList.copyOf(allDeclaredMethods);
	}

	public static Collection<Field> findDeclaredFieldsRecursively(Class<?> startingClass, Predicate<Field> filter) {
		List<Field> allDeclaredFields = new ArrayList<>();

		Class<?> nextClass = startingClass;
		while(nextClass != null && nextClass != Object.class) {
			Field[] declaredFields = nextClass.getDeclaredFields();
			for (Field declaredField : declaredFields) {
				if(filter.test(declaredField)) {
					declaredField.setAccessible(true);
					allDeclaredFields.add(declaredField);
				}
			}

			nextClass = nextClass.getSuperclass();
		}

		return ImmutableList.copyOf(allDeclaredFields);
	}

	public static Collection<String> findInjectedPackages() {
		Set<String> result = new HashSet<>();

		Enumeration<URL> resources;
		try {
			resources = ReflectionUtils.class.getClassLoader().getResources("injected.acms");
		} catch (IOException e) {
			throw new ReflectionException("Unable to load injected.acms resources", e);
		}

		while(resources.hasMoreElements()) {
			URL url = resources.nextElement();
			try(InputStream inputStream = url.openStream()) {
				try(Scanner scanner = new Scanner(inputStream)) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						result.add(line);
					}
				}
			} catch (IOException e) {
				throw new ReflectionException("Unable to open stream for " + url, e);
			}
		}

		return ImmutableSet.copyOf(result);
	}
}

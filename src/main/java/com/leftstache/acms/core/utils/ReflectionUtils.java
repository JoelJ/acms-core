package com.leftstache.acms.core.utils;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * @author Joel Johnson
 */
public class ReflectionUtils {
	public static Collection<Method> findDeclaredMethodsRecursively(Class startingClass, Predicate<Method> filter) {
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

		return allDeclaredMethods;
	}
}

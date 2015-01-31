package com.leftstache.acms.core;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Joel Johnson
 */
public interface BeanIndexer {
	void index(Class<?> type, Object bean, String name);

	<T> Bean<T> getBean(Class<T> type, String name);

	Collection<Bean<?>> getAllBeans();
}

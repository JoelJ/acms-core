package com.leftstache.acms.core;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Joel Johnson
 */
public interface BeanIndexer {
	/**
	 * Indexes and registers a bean with the given information.
	 * When called manually, you should know that no events are fired on listeners or the @OnInitialized methods
	 */
	void index(Class<?> type, Object bean, String name);

	/**
	 * Get a bean registered as the given type.
	 * If more than one bean is registered as the given type,
	 * it is undefined which will be returned and should typically
	 * only be used if only one instance of that type is registered.
	 * (For example, for getting an instance of a {@link com.leftstache.acms.core.BeanListener} or {@link com.leftstache.acms.core.BeanIndexer})
	 * When retrieving a {@link com.leftstache.acms.core.BeanListener} the implementation class should always be used.
	 */
	<T> Bean<T> getBean(Class<T> type);

	/**
	 * Gets a bean registered as the given type and name.
	 * The type is not necessarily the actual type of the registered bean,
	 * but rather the type declared in the @Inject'd method
	 */
	<T> Bean<T> getBean(Class<T> type, String name);

	/**
	 * Gets all beans that have been registered, in no particular order.
	 */
	Collection<Bean<?>> getAllBeans();
}

package com.leftstache.acms.core;

/**
 * @author Joel Johnson
 */
public interface BeanIndexer {
	<T, B extends T> void index(Class<T> type, B bean, String name);

	Object getBeanByName(String name);
}

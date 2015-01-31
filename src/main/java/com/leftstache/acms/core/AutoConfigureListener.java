package com.leftstache.acms.core;

/**
 * @author Joel Johnson
 */
public interface AutoConfigureListener {
	void autoConfiguredClassFound(Class<?> autoConfiguredClass);
	<T> T autoConfiguredClassInitialized(T autoConfiguredObject, String name);
}

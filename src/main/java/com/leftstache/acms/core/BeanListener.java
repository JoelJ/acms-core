package com.leftstache.acms.core;

/**
 * @author Joel Johnson
 */
public interface BeanListener {
	/**
	 * Called when the bean is found and needs to be created, but hasn't yet
	 * @param beanClass
	 */
	default void preInitialize(Class<?> beanClass, String name) {}

	/**
	 * Called after the bean instance has been created, but before it is registered.
	 * This is a good endpoint to inject some kind of AOP, for example.
	 *
	 * @param beanClass		The type of bean created. This is the same type as the instance, or a super class/interface of the instance.
	 * @param beanInstance	The instance that's going to be registered.
	 * @param beanName		The name the bean will be registered as.
	 * @return The actual value that should be registered. This must be an instance of the given class.
	 */
	default Object postInitialize(Class<?> beanClass, Object beanInstance, String beanName) {
		return beanInstance;
	}

	/**
	 * Called after the given bean is registered and the bean's @OnInitialized methods are called.
	 * @param bean The registered bean.
	 */
	default void postInjected(Bean<?> bean) {};
}

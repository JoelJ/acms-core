package foo.anotherpackage;

import com.leftstache.acms.core.*;

import java.util.*;

/**
 * @author Joel Johnson
 */
public class TestBeanListener implements BeanListener {
	private Set<String> preInitNames = new HashSet<>();
	private Set<String> postInitNames = new HashSet<>();
	private Set<String> postInjectedNames = new HashSet<>();

	@Override
	public void preInitialize(Class<?> beanClass, String name) {
		preInitNames.add(name);
	}

	@Override
	public Object postInitialize(Class<?> beanClass, Object beanInstance, String beanName) {
		postInitNames.add(beanName);
		return beanInstance;
	}

	@Override
	public void postInjected(Bean<?> bean) {
		postInjectedNames.add(bean.getName());
	}

	public boolean hasPreInitialized(String name) {
		return preInitNames.contains(name);
	}

	public boolean hasPostInitialized(String name) {
		return postInitNames.contains(name);
	}

	public boolean hasPostInjected(String name) {
		return postInjectedNames.contains(name);
	}
}

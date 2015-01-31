package com.leftstache.acms.core;

import com.google.common.collect.*;

import java.util.*;

/**
 * @author Joel Johnson
 */
class BeanIndexerImpl implements BeanIndexer {
	private final Map<Class<?>, Map<String, Bean<?>>> index;

	BeanIndexerImpl() {
		this.index = new HashMap<>();
	}

	@Override
	public void index(Class<?> type, Object bean, String name) {
		if(type == null) {
			throw new NullPointerException("type");
		}
		if(bean == null) {
			throw new NullPointerException("bean");
		}
		if(name == null) {
			throw new NullPointerException("name");
		}

		Map<String, Bean<?>> byName = index.get(type);
		if(byName == null) {
			byName = new HashMap<>();
			index.put(type, byName);
		}

		byName.put(name, new Bean(name, type, bean));
	}

	@Override
	public <T> Bean<T> getBean(Class<T> type, String name) {
		Map<String, Bean<?>> byName = index.get(type);
		if(byName != null) {
			return (Bean<T>)byName.get(name);
		}
		return null;
	}

	@Override
	public Collection<Bean<?>> getAllBeans() {
		List<Bean<?>> result = new ArrayList<>();

		for (Map<String, Bean<?>> byName : index.values()) {
			result.addAll(byName.values());
		}

		return ImmutableList.copyOf(result);
	}
}

package com.leftstache.acms.core;

import com.google.common.collect.*;

import java.util.*;

/**
 * @author Joel Johnson
 */
class BeanIndexerImpl implements BeanIndexer {
	private final Map<Class<?>, Map<String, Object>> index;

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

		Map<String, Object> byName = index.get(type);
		if(byName == null) {
			byName = new HashMap<>();
			index.put(type, byName);
		}

		byName.put(name, bean);
	}

	@Override
	public <T> T getBean(Class<T> type, String name) {
		Map<String, Object> byName = index.get(type);
		if(byName != null) {
			return (T)byName.get(name);
		}
		return null;
	}

	@Override
	public Collection<Object> getAllBeans() {
		List<Object> result = new ArrayList<>();

		for (Map<String, Object> byName : index.values()) {
			result.addAll(byName.values());
		}

		return ImmutableList.copyOf(result);
	}
}

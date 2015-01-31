package com.leftstache.acms.core;

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
	public Object getBean(Class<?> type, String name) {
		Map<String, Object> byName = index.get(type);
		if(byName != null) {
			return byName.get(name);
		}
		return null;
	}
}

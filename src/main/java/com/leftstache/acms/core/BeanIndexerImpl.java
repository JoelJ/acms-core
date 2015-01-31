package com.leftstache.acms.core;

import java.util.*;

/**
 * @author Joel Johnson
 */
class BeanIndexerImpl implements BeanIndexer {
	private Map<String, Object> byName;

	BeanIndexerImpl() {
		this.byName = new HashMap<>();
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

		byName.put(name, bean);
	}

	@Override
	public Object getBeanByName(String name) {
		return byName.get(name);
	}
}

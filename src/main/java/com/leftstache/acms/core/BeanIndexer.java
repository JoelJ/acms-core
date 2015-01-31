package com.leftstache.acms.core;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Joel Johnson
 */
public interface BeanIndexer {
	void index(Class<?> type, Object bean, String name);

	Object getBeanByName(String name);
}

package blah.testpackage;

import com.leftstache.acms.core.*;

import java.util.*;

/**
 * @author Joel Johnson
 */
public class InjectorStub implements Injector {
	private final Map<String, Object> indexed = new HashMap<>();

	@Override
	public void index(Object bean, String name) {
		indexed.put(name, bean);
	}

	public Object getBean(String name) {
		return indexed.get(name);
	}
}

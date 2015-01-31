package com.leftstache.acms.core;

import blah.testpackage.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Joel Johnson
 */
public class AcmsApplicationTest {
	@Test
	public void application_beans() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = app.getBeanIndexer();

		assertEquals("inject by method name", "this is an injected string", beanIndexer.getBean(String.class, "someValue").getInstance());
		assertEquals("inject by annotation value", "this is by name", beanIndexer.getBean(String.class, "byname").getInstance());
		assertEquals("inject with dependencies", "this is an injected string this is by name", beanIndexer.getBean(String.class, "someDependantValue").getInstance());

		Bean<Integer> someValue = beanIndexer.getBean(int.class, "someValue");
		assertEquals("inject duplicate by type", 10, (int)someValue.getInstance());
	}

	@Test
	public void application_complex_beans() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = app.getBeanIndexer();

		Bean<ComplexObject> complexObject = beanIndexer.getBean(ComplexObject.class, "complexObject");
		ComplexObject instance = complexObject.getInstance();

		assertNotNull("actual object", complexObject);
		assertEquals("int value", 10, instance.getSomeIntValue());
		assertEquals("string value", "this is an injected string", instance.getSomeValue());

		assertEquals("missing primitive", 0, instance.getMissingPrimitiveValue());
		assertEquals("missing object", null, instance.getMissingObject());
	}
}

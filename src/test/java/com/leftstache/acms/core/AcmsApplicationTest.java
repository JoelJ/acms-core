package com.leftstache.acms.core;

import blah.testpackage.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Joel Johnson
 */
public class AcmsApplicationTest {
	@Test
	public void application_beans() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = app.getBeanIndexer();

		assertEquals("inject by method name", "this is an injected string", beanIndexer.getBean(String.class, "someValue"));
		assertEquals("inject by annotation value", "this is by name", beanIndexer.getBean(String.class, "byname"));
		assertEquals("inject with dependencies", "this is an injected string this is by name", beanIndexer.getBean(String.class, "someDependantValue"));

		int someValue = beanIndexer.getBean(int.class, "someValue");
		assertEquals("inject duplicate by type", 10, someValue);
	}

	@Test
	public void application_complex_beans() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = app.getBeanIndexer();

		ComplexObject complexObject = beanIndexer.getBean(ComplexObject.class, "complexObject");
		assertEquals("int value", 10, complexObject.getSomeIntValue());
		assertEquals("string value", "someValue", complexObject.getSomeIntValue());
	}
}

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

		assertEquals("inject by method name", "this is an injected string", beanIndexer.getBeanByName("someValue"));
		assertEquals("inject by annotation value", "this is by name", beanIndexer.getBeanByName("byname"));
		assertEquals("inject with dependencies", "this is an injected string this is by name", beanIndexer.getBeanByName("someDependantValue"));
	}
}

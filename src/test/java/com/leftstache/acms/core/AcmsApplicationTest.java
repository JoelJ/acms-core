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
		AcmsApplication run = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = run.getBeanIndexer();

		assertEquals("this is an injected string", beanIndexer.getBeanByName("someValue"));
		assertEquals("this is by name", beanIndexer.getBeanByName("byname"));
		assertEquals("this is an injected string this is by name", beanIndexer.getBeanByName("someDependantValue"));
	}
}

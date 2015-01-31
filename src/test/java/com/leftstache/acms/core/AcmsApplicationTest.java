package com.leftstache.acms.core;

import blah.testpackage.*;
import blah.testpackage.injectable.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Joel Johnson
 */
public class AcmsApplicationTest {
	@Test
	public void testApplication_packageScanning() {
		AcmsApplication acmsApplication = new AcmsApplication(
			TestApplication.class,
			new InjectorStub(),
			Arrays.asList("blah.testpackage")
		);
		Set<Class<? extends AutoConfigureListener>> autoconfiguredTypes = acmsApplication.getAutoconfiguredTypes();
		assertTrue(autoconfiguredTypes.contains(AutoConfigureListenerStub.class));

		Set<Class<?>> injectedAppTypes = acmsApplication.getInjectedAppTypes();
		assertTrue(injectedAppTypes.contains(TestInjectable.class));
	}

	@Test
	public void testApplication_injectableIndexing() throws InstantiationException, IllegalAccessException {
		InjectorStub injector = new InjectorStub();
		AcmsApplication acmsApplication = new AcmsApplication(
			TestApplication.class,
			injector,
			Arrays.asList("blah.testpackage")
		);

		acmsApplication.start(); // this will find all the injectables and index them.

		assertNotNull(injector.getBean("testInjectable"));
		assertNotNull(injector.getBean("byname"));
		assertTrue(injector.getBean("testInjectable") instanceof TestInjectable);
		assertTrue(injector.getBean("byname") instanceof TestInjectableByName);
	}

	@Test
	public void testApplication_resourceScanning() {
		AcmsApplication acmsApplication = AcmsApplication.run(TestApplication.class);
		Set<Class<? extends AutoConfigureListener>> autoconfiguredTypes = acmsApplication.getAutoconfiguredTypes();
		assertTrue(autoconfiguredTypes.contains(AutoConfigureListenerStub.class));

		Set<Class<?>> injectedAppTypes = acmsApplication.getInjectedAppTypes();
		assertTrue(injectedAppTypes.contains(TestInjectable.class));
	}
}

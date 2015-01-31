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
	public void testApplication() {
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
}

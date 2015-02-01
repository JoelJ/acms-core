package com.leftstache.acms.core;

import blah.testpackage.*;
import foo.anotherpackage.*;
import org.junit.*;

import static org.junit.Assert.*;

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

	@Test
	public void application_onInitialized() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = app.getBeanIndexer();

		Bean<ComplexObject> complexObject = beanIndexer.getBean(ComplexObject.class, "complexObject");
		assertEquals("value set OnInitialize", "initialized", complexObject.getInstance().getInitializedOnInit());
	}

	@Test
	public void application_externalBean() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = app.getBeanIndexer();

		Bean<TestAutoConfiguredImpl> autoConfiguredBean = beanIndexer.getBean(TestAutoConfiguredImpl.class, "testAutoConfiguredImpl");
		assertEquals("value set OnInitialize", "initialized", autoConfiguredBean.getInstance().getOnInitializedValue());

		Bean<String> autoConfiguredString = beanIndexer.getBean(String.class, "someAutoConfiguredValue");
		assertEquals("method bean", autoConfiguredString.getInstance(), "some autoconfigured value");
	}

	@Test
	public void application_includesExternal() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = app.getBeanIndexer();

		Bean<String> autoConfiguredString = beanIndexer.getBean(String.class, "includesExternal");
		assertEquals("includes external bean", autoConfiguredString.getInstance(), "includes external some autoconfigured value");

		Bean<TestAutoConfiguredImplCustomName> autoConfiguredObject = beanIndexer.getBean(TestAutoConfiguredImplCustomName.class, "bacon");
		assertNotNull("includes external bean with custom name", autoConfiguredObject);
		assertNotNull("includes external bean with custom name", autoConfiguredObject.getInstance());
	}

	@Test
	public void application_listeners() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		BeanIndexer beanIndexer = app.getBeanIndexer();

		Bean<TestBeanListener> testBeanListenerBean = beanIndexer.getBean(TestBeanListener.class);
		TestBeanListener testBeanListener = testBeanListenerBean.getInstance();

		// standard injected objects should have all three events fired
		assertTrue("ComplexObject event", testBeanListener.hasPreInitialized("complexObject"));
		assertTrue("ComplexObject event", testBeanListener.hasPostInitialized("complexObject"));
		assertTrue("ComplexObject event", testBeanListener.hasPostInjected("complexObject"));

		// BeanListeners only have the PostInjected events fired
		assertFalse("BeanListener event", testBeanListener.hasPreInitialized("testBeanListener"));
		assertFalse("BeanListener event", testBeanListener.hasPostInitialized("testBeanListener"));
		assertTrue("BeanListener event", testBeanListener.hasPostInjected("testBeanListener"));

		// BeanIndexer only have the PostInjected events fired
		assertFalse("BeanIndexer event", testBeanListener.hasPreInitialized("beanIndexer"));
		assertFalse("BeanIndexer event", testBeanListener.hasPostInitialized("beanIndexer"));
		assertTrue("BeanIndexer event", testBeanListener.hasPostInjected("beanIndexer"));
	}

	@Test
	public void applicationEvents() {
		AcmsApplication app = AcmsApplication.run(TestApplication.class);
		TestApplication application = (TestApplication) app.getApplication();

		boolean[] closeCalled = new boolean[]{ false };
		new Thread(() -> {
			try {
				// give the main thread time to call start, and the events to fire
				do {
					Thread.sleep(100);
				} while(!app.isStarted());

				assertTrue("Start event called", application.isStartEventCalled());
				assertFalse("Close event not called", application.isCloseEventCalled());

				closeCalled[0] = true;
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				app.close();
			}
		}).start();

		assertFalse("Start event not called", application.isStartEventCalled());
		assertFalse("Close event not called", application.isCloseEventCalled());

		app.start();
		assertTrue("the close method was called", closeCalled[0]);

		Bean<ApplicationListener> applicationListener = app.getBeanIndexer().getBean(ApplicationListener.class, "applicationListener");
		assertNotNull(applicationListener);

		assertTrue("Start event called", application.isStartEventCalled());
		assertTrue("Close event called", application.isCloseEventCalled());
	}
}

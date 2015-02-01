package blah.testpackage;

import com.leftstache.acms.core.*;
import com.leftstache.acms.core.annotation.*;

/**
 * @author Joel Johnson
 */
@AutoConfiguredApp
public class TestApplication {
	@Inject
	public String someValue() {
		return "this is an injected string";
	}

	@Inject("byname")
	public String someValueByName() {
		return "this is by name";
	}

	@Inject
	public String someDependantValue(String someValue, String byname) {
		return someValue + " " + byname;
	}

	@Inject("someValue")
	public int someIntValue() {
		return 10;
	}

	@Inject
	public ComplexObject complexObject() {
		return new ComplexObject();
	}

	@Inject
	public String includesExternal(String someAutoConfiguredValue) {
		return "includes external " + someAutoConfiguredValue;
	}

	private boolean startEventCalled = false;
	private boolean closeEventCalled = false;

	@Inject
	public ApplicationListener applicationListener() {
		return new ApplicationListener() {
			@Override
			public void applicationStart(Object application) {
				startEventCalled = true;
			}

			@Override
			public void applicationClose(Object application) {
				closeEventCalled = true;
			}
		};
	}

	public boolean isStartEventCalled() {
		return startEventCalled;
	}

	public boolean isCloseEventCalled() {
		return closeEventCalled;
	}
}
package blah.testpackage;

import com.leftstache.acms.core.*;

/**
 * @author Joel Johnson
 */
public class AutoConfigureListenerStub implements AutoConfigureListener {
	@Override
	public void autoConfiguredClassFound(Class<?> autoConfiguredClass) {

	}

	@Override
	public <T> T autoConfiguredClassInitialized(T autoConfiguredObject, String name) {
		return null;
	}
}

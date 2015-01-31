package foo.anotherpackage;

import com.leftstache.acms.core.annotation.*;

/**
 * @author Joel Johnson
 */
@Inject
public class TestAutoConfiguredImpl {
	private String onInitializedValue = "not initialized!";

	@OnInitialized
	public void onInitialized() {
		onInitializedValue = "initialized";
	}

	@Inject
	public String someAutoConfiguredValue() {
		return "some autoconfigured value";
	}

	public String getOnInitializedValue() {
		return onInitializedValue;
	}
}

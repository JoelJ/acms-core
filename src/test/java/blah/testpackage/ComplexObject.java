package blah.testpackage;

import com.leftstache.acms.core.annotation.*;

/**
 * @author Joel Johnson
 */
public class ComplexObject {
	@Inject
	private String someValue;

	@Inject("someValue")
	private int someIntValue;

	public String getSomeValue() {
		return someValue;
	}

	public int getSomeIntValue() {
		return someIntValue;
	}
}

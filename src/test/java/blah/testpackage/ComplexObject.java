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

	@Inject
	private long missingPrimitiveValue;

	@Inject
	private Object missingObject = null;

	public String getSomeValue() {
		return someValue;
	}

	public int getSomeIntValue() {
		return someIntValue;
	}

	public long getMissingPrimitiveValue() {
		return missingPrimitiveValue;
	}

	public Object getMissingObject() {
		return missingObject;
	}
}

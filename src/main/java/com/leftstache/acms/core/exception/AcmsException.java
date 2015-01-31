package com.leftstache.acms.core.exception;

/**
 * @author Joel Johnson
 */
public class AcmsException extends RuntimeException {
	public AcmsException() {
	}

	public AcmsException(String message) {
		super(message);
	}

	public AcmsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AcmsException(Throwable cause) {
		super(cause);
	}

	public AcmsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

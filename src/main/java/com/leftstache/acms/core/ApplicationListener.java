package com.leftstache.acms.core;

/**
 * @author Joel Johnson
 */
public interface ApplicationListener {
	void applicationStart(Object application);
	void applicationClose(Object application);
}

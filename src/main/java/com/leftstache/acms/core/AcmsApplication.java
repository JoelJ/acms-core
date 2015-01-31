package com.leftstache.acms.core;

import com.google.common.collect.*;
import com.leftstache.acms.core.annotation.*;
import com.leftstache.acms.core.exception.*;
import org.reflections.*;

import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Joel Johnson
 */
public class AcmsApplication {
	private final Class<?> applicationClass;
	private final BeanIndexer beanIndexer;

	public AcmsApplication(Class<?> applicationClass, BeanIndexer beanIndexer) {
		this.applicationClass = applicationClass;
		this.beanIndexer = beanIndexer;
	}

	public static AcmsApplication run(Class<?> applicationClass) {
		if(applicationClass == null) {
			throw new NullPointerException("applicationClass");
		}

		if(applicationClass.getAnnotation(AutoConfiguredApp.class) == null) {
			throw new AcmsException("Unabled to initialize app not annotated with AutoConfiguredApp");
		}

		AcmsApplication acmsApplication;
		acmsApplication = new AcmsApplication(applicationClass, new BeanIndexerImpl());
		acmsApplication.start();
		return acmsApplication;
	}

	void start() {

	}

	public Class<?> getApplicationClass() {
		return applicationClass;
	}

	public BeanIndexer getBeanIndexer() {
		return beanIndexer;
	}
}

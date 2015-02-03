package com.smartbear.soapui.spring;

import java.util.Collections;
import java.util.Map;

public class SoapUiSpringTest {

	private SoapUiTestSuiteProvider testSuiteProvider;

	/**
	 * Called by SoapUiTestSuiteProvider with reflection private method call
	 */
	public void setReader(SoapUiTestSuiteProvider testSuiteProvider) {
		this.testSuiteProvider = testSuiteProvider;
	}

	/**
	 * Called by generated JUnit test class.
	 * 
	 * @param id
	 *            unique Test id
	 * @throws Throwable
	 */
	public void launchTest(String id) throws Throwable {
		testSuiteProvider.getTest(id, getTestProperties()).runBare();
	}

	public Map<String, String> getTestProperties() {
		return Collections.emptyMap();
	}

}

package com.smartbear.soapui.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Condition;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;

public class SoapUiTestCase extends junit.framework.TestCase {

	private TestCase testCase;

	private final String uniqueId;

	public SoapUiTestCase(TestCase testCase, String uniqueId) {
		super(testCase.getName());
		this.testCase = testCase;
		this.uniqueId = uniqueId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	protected void runTest() throws Throwable {
		WsdlTestCaseRunner runner = (WsdlTestCaseRunner) testCase.run(new PropertiesMap(), false);
		List<TestStepResult> results = runner.getResults();

		assertThat(results).overridingErrorMessage(errorMessage(results)).are(new Condition<TestStepResult>() {
			@Override
			public boolean matches(TestStepResult value) {
				return TestStepStatus.OK.equals(value.getStatus());
			}
		});

		assertThat(runner.getStatus()).isEqualTo(Status.FINISHED);
	}

	private String errorMessage(List<TestStepResult> results) {
		StringBuilder sb = new StringBuilder();

		for (TestStepResult testStepResult : results) {
			if (!TestStepStatus.OK.equals(testStepResult.getStatus())) {
				sb.append(testStepResultErrorMessage(testStepResult));
			}
		}

		return sb.toString();
	}

	private String testStepResultErrorMessage(TestStepResult testStepResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\nStep <" + testStepResult.getTestStep().getName() + "> : " + testStepResult.getStatus() + "\n");

		String[] messages = testStepResult.getMessages();
		for (String message : messages) {
			sb.append(message);
		}
		return sb.toString();
	}

}
package com.smartbear.soapui.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

@Ignore
public class SoapUiTestCase extends junit.framework.TestCase {

	Logger LOGGER = LoggerFactory.getLogger(SoapUiTestCase.class);

	private TestCase testCase;

	private final String uniqueId;

	public static String convertAccents(String accent) {
		return Normalizer.normalize(accent, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

	public SoapUiTestCase(String testSuiteName, TestCase testCase, String uniqueId) {
		super(convertAccents(testSuiteName + " - " + testCase.getName()));
		this.testCase = testCase;
		this.uniqueId = uniqueId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	protected void runTest() throws Throwable {
		WsdlTestCaseRunner runner = (WsdlTestCaseRunner) testCase.run(new PropertiesMap(), false);
		List<TestStepResult> results = runner.getResults();
		LOGGER.debug(getMessages(testCase.getName(), results));

		Collection<TestStepResult> filteredResult = Collections2.filter(results, new Predicate<TestStepResult>() {
			public boolean apply(TestStepResult testStepResult) {
				return !TestStepStatus.OK.equals(testStepResult.getStatus()) && !TestStepStatus.UNKNOWN.equals(testStepResult.getStatus());
			}
		});

		assertThat(filteredResult).overridingErrorMessage(getMessages(testCase.getName(), filteredResult)).isEmpty();

		Map<String, TestStep> emptyTestSteps = Collections.emptyMap();
		Map<String, TestStep> allTestSteps = runner.getTestRunnable() != null ? runner.getTestRunnable().getTestSteps() : emptyTestSteps;
		if (results.size() != allTestSteps.size()) {
			assertAllTestSteps(allTestSteps);
		}

		assertThat(runner.getStatus()).isEqualTo(Status.FINISHED);

	}

	private void assertAllTestSteps(Map<String, TestStep> allTestSteps) {
		for (TestStep testStep : allTestSteps.values()) {

			if (testStep instanceof RestTestRequestStep) {
				RestTestRequestStep restTestRequestStep = (RestTestRequestStep) testStep;

				if (AssertionStatus.FAILED.equals(restTestRequestStep.getAssertionStatus())) {
					for (TestAssertion testAssertion : restTestRequestStep.getAssertionList()) {
						assertThat(testAssertion.getStatus()).overridingErrorMessage(getTestStepMessage(testAssertion, testStep)).isEqualTo(
								AssertionStatus.VALID);
					}
				}
			}
		}
	}

	private String getTestStepMessage(TestAssertion testAssertion, TestStep testStep) {
		StringBuilder sb = new StringBuilder();
		sb.append("Expected :VALID but was ").append(testAssertion.getStatus());
		sb.append("\n TestStep: ").append(testStep.getName()).append("\n Assertion: ").append(testAssertion.getName());
		String reasonForFailure = null;
		if (testAssertion.getErrors() != null) {
			reasonForFailure = testAssertion.getErrors()[0].getMessage();
		}
		sb.append("\n").append(reasonForFailure);
		return sb.toString();
	}

	private String getMessages(String testCaseName, Collection<TestStepResult> results) {
		StringBuilder sb = new StringBuilder();
		sb.append("Results for test case [" + testCaseName + "]");
		for (TestStepResult testStepResult : results) {
			sb.append(logTestStep(testStepResult));
		}

		return sb.toString();
	}

	public String logTestStep(TestStepResult testStepResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n---------- Test Step BEGIN ----------\n");
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter, true);
		testStepResult.writeTo(printWriter);
		sb.append(stringWriter.toString());
		sb.append("\n---------- Test Step END ------------\n");
		return sb.toString();
	}

	public void clear() {
		WsdlTestCase wsdlTestCase = (WsdlTestCase) testCase;
		while (wsdlTestCase.getLoadTestCount() > 0) {
			wsdlTestCase.removeLoadTest(wsdlTestCase.getLoadTestAt(0));
		}

		while (wsdlTestCase.getTestStepCount() > 0)
			wsdlTestCase.removeTestStep(wsdlTestCase.getTestStepAt(0));

		testCase = null;
	}
}
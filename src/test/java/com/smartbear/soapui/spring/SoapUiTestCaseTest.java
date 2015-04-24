package com.smartbear.soapui.spring;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.RestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;

public class SoapUiTestCaseTest {

	@Test
	public void check_runTest_onTestOk() throws Throwable {

		TestCase testCase = mock(TestCase.class);
		WsdlTestCaseRunner runner = mock(WsdlTestCaseRunner.class);
		TestStepResult testStepResult = mock(TestStepResult.class);
		List<TestStepResult> results = Arrays.asList(testStepResult);

		when(testCase.run(any(PropertiesMap.class), eq(false))).thenReturn(runner);
		when(runner.getResults()).thenReturn(results);
		when(testStepResult.getStatus()).thenReturn(TestStepStatus.OK);
		when(runner.getStatus()).thenReturn(Status.FINISHED);
		when(testCase.getName()).thenReturn("Name of the test");

		SoapUiTestCase soapUiTestCase = new SoapUiTestCase("TestSuite Name", testCase, "uniqueId");

		soapUiTestCase.runBare();
	}

	@Test
	public void check_runTest_onTestUnknown() throws Throwable {

		TestCase testCase = mock(TestCase.class);
		WsdlTestCaseRunner runner = mock(WsdlTestCaseRunner.class);
		TestStepResult testStepResult = mock(TestStepResult.class);
		List<TestStepResult> results = Arrays.asList(testStepResult);

		when(testCase.run(any(PropertiesMap.class), eq(false))).thenReturn(runner);
		when(runner.getResults()).thenReturn(results);
		when(testStepResult.getStatus()).thenReturn(TestStepStatus.UNKNOWN);
		when(runner.getStatus()).thenReturn(Status.FINISHED);
		when(testCase.getName()).thenReturn("Name of the test");

		SoapUiTestCase soapUiTestCase = new SoapUiTestCase("TestSuite Name", testCase, "uniqueId");

		soapUiTestCase.runBare();
	}

	@Test
	public void check_runTest_onTestCaseKo() throws Throwable {

		TestCase testCase = mock(TestCase.class);
		WsdlTestCaseRunner runner = mock(WsdlTestCaseRunner.class);
		RestTestRequestStep testStep = mock(RestTestRequestStep.class);

		RestRequestStepResult testStepResult = mock(RestRequestStepResult.class);
		when(testStepResult.getStatus()).thenReturn(TestStepStatus.FAILED);
		when(testCase.run(any(PropertiesMap.class), eq(false))).thenReturn(runner);
		when(testStep.getName()).thenReturn("test step name");
		when(runner.getStatus()).thenReturn(Status.FINISHED);
		when(testCase.getName()).thenReturn("Name of the test");
		when(runner.getResults()).thenReturn(Arrays.asList((TestStepResult) testStepResult));

		SoapUiTestCase soapUiTestCase = new SoapUiTestCase("TestSuite Name", testCase, "uniqueId");
		try {
			soapUiTestCase.runBare();
		} catch (AssertionError e) {
			Assertions.assertThat(e.getMessage()).isEqualTo(
					"Results for test case [Name of the test]" + "\n---------- Test Step BEGIN ----------\n"
							+ "\n---------- Test Step END ------------\n");

			return;
		}
		failBecauseExceptionWasNotThrown(AssertionError.class);

	}

	@Test
	public void check_runTest_onTestCaseKo_with_differences_between_results_and_testSteps_details() throws Throwable {

		// Set
		TestCase testCase = mock(TestCase.class);
		when(testCase.getName()).thenReturn("Name of the test");
		WsdlTestCaseRunner runner = mock(WsdlTestCaseRunner.class);
		when(runner.getStatus()).thenReturn(Status.FAILED);
		when(testCase.run(any(PropertiesMap.class), eq(false))).thenReturn(runner);
		RestTestRequestStep testStep = mock(RestTestRequestStep.class);
		when(testStep.getName()).thenReturn("test step name");
		RestRequestStepResult testStepResult = mock(RestRequestStepResult.class);
		when(testStepResult.getStatus()).thenReturn(TestStepStatus.OK);

		when(runner.getResults()).thenReturn(Arrays.asList((TestStepResult) testStepResult));

		WsdlTestCase wsdlTestCase = mock(WsdlTestCase.class);
		when(runner.getTestRunnable()).thenReturn(wsdlTestCase);
		Map<String, TestStep> testSteps = new HashMap<String, TestStep>();
		TestStep step1 = mock(TestStep.class);
		testSteps.put("step1", step1);
		RestTestRequestStep step2 = mock(RestTestRequestStep.class);
		testSteps.put("step2", step2);
		RestTestRequestStep restTestRequestStep2 = (RestTestRequestStep) step2;
		when(restTestRequestStep2.getAssertionStatus()).thenReturn(AssertionStatus.FAILED);

		TestAssertion testAssertion2 = mock(TestAssertion.class);
		TestAssertion testAssertion1 = mock(TestAssertion.class);
		when(testAssertion1.getStatus()).thenReturn(AssertionStatus.FAILED);
		when(testAssertion2.getStatus()).thenReturn(AssertionStatus.FAILED);

		List<TestAssertion> assertions = Arrays.asList(testAssertion1, testAssertion2);
		when(restTestRequestStep2.getAssertionList()).thenReturn(assertions);
		when(wsdlTestCase.getTestSteps()).thenReturn(testSteps);
		SoapUiTestCase soapUiTestCase = new SoapUiTestCase("TestSuite Name", testCase, "uniqueId");

		// Test
		try {
			soapUiTestCase.runTest();
		} catch (AssertionError e) {
			// Assert
			Assertions.assertThat(e.getMessage()).startsWith("Expected :VALID");
			return;
		}
		failBecauseExceptionWasNotThrown(AssertionError.class);

	}
}

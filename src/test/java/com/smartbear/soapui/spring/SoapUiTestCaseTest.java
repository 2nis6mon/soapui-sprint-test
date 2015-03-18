package com.smartbear.soapui.spring;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.support.PropertiesMap;
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

		SoapUiTestCase soapUiTestCase = new SoapUiTestCase(testCase, "uniqueId");

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

		SoapUiTestCase soapUiTestCase = new SoapUiTestCase(testCase, "uniqueId");

		soapUiTestCase.runBare();
	}

	@Test
	public void check_runTest_onTestCaseKo() throws Throwable {

		TestCase testCase = mock(TestCase.class);
		WsdlTestCaseRunner runner = mock(WsdlTestCaseRunner.class);
		TestStepResult testStepResult = mock(TestStepResult.class);
		TestStep testStep = mock(TestStep.class);

		when(testCase.run(any(PropertiesMap.class), eq(false))).thenReturn(runner);
		when(runner.getResults()).thenReturn(Arrays.asList(testStepResult));
		when(testStepResult.getStatus()).thenReturn(TestStepStatus.FAILED);
		when(testStepResult.getMessages()).thenReturn(new String[] { "failed step" });
		when(testStepResult.getTestStep()).thenReturn(testStep);
		when(testStep.getName()).thenReturn("test step name");
		when(runner.getStatus()).thenReturn(Status.FINISHED);
		when(testCase.getName()).thenReturn("Name of the test");

		SoapUiTestCase soapUiTestCase = new SoapUiTestCase(testCase, "uniqueId");
		try {
			soapUiTestCase.runBare();
		} catch (AssertionError e) {
			Assertions.assertThat(e.getMessage()).isEqualTo("\n\nStep <test step name> : FAILED\nfailed step");
			return;
		}
		failBecauseExceptionWasNotThrown(AssertionError.class);

	}
}

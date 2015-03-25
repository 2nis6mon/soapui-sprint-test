package com.smartbear.soapui.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Normalizer;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Ignore;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;

@Ignore
public class SoapUiTestCase extends junit.framework.TestCase {

	private TestCase testCase;

	private final String uniqueId;

	public static String convertAccents(String accent) {
		return Normalizer.normalize(accent, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

	public SoapUiTestCase(TestCase testCase, String uniqueId) {
		super(convertAccents(testCase.getName()));
		this.testCase = testCase;
		this.uniqueId = uniqueId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	protected void runTest() throws Throwable {
		WsdlTestCaseRunner runner = (WsdlTestCaseRunner) testCase.run(new PropertiesMap(), false);
		List<TestStepResult> results = runner.getResults();

		assertThat(results).overridingErrorMessage(errorMessage(testCase.getName(), results)).are(new Condition<TestStepResult>() {
			@Override
			public boolean matches(TestStepResult value) {
				return TestStepStatus.OK.equals(value.getStatus()) || TestStepStatus.UNKNOWN.equals(value.getStatus());
			}
		});

		assertThat(runner.getStatus()).isEqualTo(Status.FINISHED);
	}

	private String errorMessage(String testCaseName, List<TestStepResult> results) {
		StringBuilder sb = new StringBuilder();
		sb.append("Results for test case [" + testCaseName + "]");
		for (TestStepResult testStepResult : results) {
			if (!TestStepStatus.OK.equals(testStepResult.getStatus()) && !TestStepStatus.UNKNOWN.equals(testStepResult.getStatus())) {
				sb.append("\n---------- Test Step BEGIN ----------");
				StringWriter stringWriter = new StringWriter();
				PrintWriter printWriter = new PrintWriter(stringWriter, true);
				testStepResult.writeTo(printWriter);
				sb.append(stringWriter.toString());
				sb.append("\n---------- Test Step END ------------");
			}
		}

		return sb.toString();
	}

}
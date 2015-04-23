package com.smartbear.soapui.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.smartbear.soapui.spring.example.ExistingProjectSoapUiTestClass;

public class SoapUiTestSuiteProviderTest {

	@Test
	public void check_testProvider() throws Exception {
		SoapUiTestSuiteProvider soapUiTestSuiteProvider = new SoapUiTestSuiteProvider(ExistingProjectSoapUiTestClass.class);

		assertThat(soapUiTestSuiteProvider).isNotNull();
		assertThat(soapUiTestSuiteProvider.getTestMethods()).hasSize(1);
		assertThat(soapUiTestSuiteProvider.getTestMethods().get(0).getName()).isEqualTo("TestSuiteMock - TestCase");
		assertThat(soapUiTestSuiteProvider.newTestClassInstance()).isInstanceOf(ExistingProjectSoapUiTestClass.class);

	}

	@Test
	public void check_testProvider_notImplements_SoapUiSpringTest() throws Exception {
		SoapUiTestSuiteProvider soapUiTestSuiteProvider = new SoapUiTestSuiteProvider(ExistingProjectSoapUiTestClass.class);

		assertThat(soapUiTestSuiteProvider).isNotNull();
		assertThat(soapUiTestSuiteProvider.getTestMethods()).hasSize(1);
		assertThat(soapUiTestSuiteProvider.getTestMethods().get(0).getName()).isEqualTo("TestSuiteMock - TestCase");
		assertThat(soapUiTestSuiteProvider.newTestClassInstance()).isInstanceOf(ExistingProjectSoapUiTestClass.class);

	}

}

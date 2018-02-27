package com.smartbear.soapui.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.smartbear.soapui.spring.example.ExistingProjectSoapUiTestClass;

public class SoapUiSpringTestUtilsTest {

	public class WithoutSoapUiProjectSoapUiTestClass {

	}

	@SoapUiProject("not_existing_project.xml")
	public class NotExistingProjectSoapUiTestClass {

	}

	@Test(expected = SoapUiSpringTestException.class)
	public void check_createWsdlProjectPro_withoutAnnotationClass() {
		SoapUiSpringTestUtils.createWsdlProjectPro(WithoutSoapUiProjectSoapUiTestClass.class);
	}

	@Test(expected = SoapUiSpringTestException.class)
	public void check_createWsdlProjectPro_fileNotFound() {
		SoapUiSpringTestUtils.createWsdlProjectPro(NotExistingProjectSoapUiTestClass.class);
	}

	@Test
	public void check_createWsdlProjectPro_existingProject() {
		List<WsdlProject> projects = SoapUiSpringTestUtils.createWsdlProjectPro(ExistingProjectSoapUiTestClass.class);
		assertThat(projects).isNotEmpty();
	}

	@Test
	public void check_getSoapUiTestCases() {
		List<WsdlProject> projects = SoapUiSpringTestUtils.createWsdlProjectPro(ExistingProjectSoapUiTestClass.class);
		assertThat(projects).isNotEmpty();
		List<SoapUiTestCase> testCases = SoapUiSpringTestUtils.getSoapUiTestCases(projects.get(0));
		assertThat(testCases).isNotEmpty();
	}

	@Test
	public void check_setWsdlProjectProProperties() {
		WsdlProject projectMock = Mockito.mock(WsdlProject.class);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("property1", "value1");
		properties.put("property2", "value2");

		SoapUiSpringTestUtils.setWsdlProjectProProperties(projectMock, properties);

		verify(projectMock, times(1)).setPropertyValue("property1", "value1");
		verify(projectMock, times(1)).setPropertyValue("property2", "value2");
	}
}

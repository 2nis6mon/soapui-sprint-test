package com.smartbear.soapui.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ws.security.util.UUIDGenerator;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;
import com.google.common.base.Strings;

public class SoapUiSpringTestUtils {

	public static WorkspaceImpl workspace = null;

	public static WsdlProjectPro createWsdlProjectPro(Class<?> klass) {
		File projectFile = getProjectFile(klass);
		WorkspaceImpl workspace = getWorkspace();
		WsdlProjectPro project = new WsdlProjectPro(projectFile.getAbsolutePath(), workspace);
		@SuppressWarnings("unchecked")
		List<Project> projectList = (List<Project>) workspace.getProjectList();
		projectList.add(project);
		return project;
	}

	private static WorkspaceImpl getWorkspace() {

		if (workspace == null) {
			String workspaceFile = "target" + File.separatorChar + SoapUI.DEFAULT_WORKSPACE_FILE;

			StringToStringMap projectOptions = new StringToStringMap(1);
			try {
				workspace = (WorkspaceImpl) WorkspaceFactory.getInstance().openWorkspace(workspaceFile, projectOptions);
			} catch (SoapUIException e) {
				throw new SoapUiSpringTestException(e);
			}
		}
		return workspace;
	}

	public static List<SoapUiTestCase> getSoapUiTestCases(WsdlProjectPro project) {

		List<SoapUiTestCase> suite = new ArrayList<SoapUiTestCase>();

		if (!project.isDisabled()) {
			List<com.eviware.soapui.model.testsuite.TestSuite> testSuiteList = project.getTestSuiteList();
			for (com.eviware.soapui.model.testsuite.TestSuite testSuite : testSuiteList) {
				if (!testSuite.isDisabled()) {
					suite.addAll(getTestCases(testSuite));
				}
			}
		}

		return suite;
	}

	public static void setWsdlProjectProProperties(WsdlProjectPro project, Map<String, String> properties) {
		for (String key : properties.keySet()) {
			project.setPropertyValue(key, properties.get(key));
		}
	}

	private static List<SoapUiTestCase> getTestCases(com.eviware.soapui.model.testsuite.TestSuite testSuite) {
		List<SoapUiTestCase> testCaseList = new ArrayList<SoapUiTestCase>();
		List<TestCase> testCases = testSuite.getTestCaseList();
		for (TestCase testCase : testCases) {
			if (!testCase.isDisabled()) {
				testCaseList.add(new SoapUiTestCase(testSuite.getName(), testCase, UUIDGenerator.getUUID()));
			}
		}

		return testCaseList;
	}

	private static File getProjectFile(Class<?> testClass) {
		SoapUiProject soapUiProjectAnnotation = testClass.getAnnotation(SoapUiProject.class);
		if (soapUiProjectAnnotation == null || Strings.isNullOrEmpty(soapUiProjectAnnotation.value())) {
			throw new SoapUiSpringTestException("Missing annotation \'@" + SoapUiProject.class.getSimpleName() + "\' on class ["
					+ testClass.getName() + "]");
		}

		final File file = new File(soapUiProjectAnnotation.value());
		if (!file.exists()) {
			throw new SoapUiSpringTestException("Missing file " + file.getAbsolutePath());
		}
		return file;
	}

}

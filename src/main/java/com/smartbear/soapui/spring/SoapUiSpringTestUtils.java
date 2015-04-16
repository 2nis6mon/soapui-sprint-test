package com.smartbear.soapui.spring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.ws.security.util.UUIDGenerator;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.model.testsuite.TestCase;
import com.google.common.base.Strings;

public class SoapUiSpringTestUtils {

	public static final String PREFIX = "projectFile_";
	public static final String SUFFIX = ".xml";

	public static WsdlProjectPro createWsdlProjectPro(Class<?> klass) {
		File tmpFile = getProjectFile(klass);
		return new WsdlProjectPro(tmpFile.getAbsolutePath());
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
				testCaseList.add(new SoapUiTestCase(testCase, UUIDGenerator.getUUID()));
			}
		}

		return testCaseList;
	}

	// TODO DSI : pas nécessaire si on integre la version 5.1.2 de soapui-pro
	private static File getProjectFile(Class<?> testClass) {
		InputStream in = null;
		FileOutputStream out = null;
		SoapUiProject soapUiProjectAnnotation = testClass.getAnnotation(SoapUiProject.class);
		if (soapUiProjectAnnotation == null || Strings.isNullOrEmpty(soapUiProjectAnnotation.value())) {
			throw new SoapUiSpringTestException("Missing annotation \'@" + SoapUiProject.class.getSimpleName() + "\' on class ["
					+ testClass.getName() + "]");
		}

		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(soapUiProjectAnnotation.value());
			final File tempFile = File.createTempFile(PREFIX + soapUiProjectAnnotation.value(), SUFFIX);
			tempFile.deleteOnExit();
			out = new FileOutputStream(tempFile);
			IOUtils.copy(in, out);
			return tempFile;
		} catch (Exception e) {
			throw new SoapUiSpringTestException("Error loading soapui project file from class " + testClass.getCanonicalName(), e);
		} finally {
			if (in != null) {
				IOUtils.closeQuietly(in);
			}
			if (out != null) {
				IOUtils.closeQuietly(out);
			}
		}
	}

}

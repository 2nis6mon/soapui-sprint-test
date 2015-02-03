package com.smartbear.soapui.spring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.ws.security.util.UUIDGenerator;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.model.testsuite.TestCase;

public class SoapUiSpringTestUtils {

	public static final String PREFIX = "projectFile_";
	public static final String SUFFIX = ".xml";

	public static WsdlProjectPro crateWsdlProjectPro(Class<?> klass) {
		File tmpFile = getProjectFile(klass);
		if (tmpFile == null) {
			throw new SoapUiSpringTestException("Error loading project file");
		}

		return new WsdlProjectPro(tmpFile.getAbsolutePath());
	}

	public static List<SoapUiTestCase> suite(WsdlProjectPro project) {

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
			project.removeProperty(key);
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
		try {
			SoapUiProject projectName = getAnnotation(testClass, SoapUiProject.class);
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(projectName.value());
			final File tempFile = File.createTempFile(PREFIX + projectName.value(), SUFFIX);
			tempFile.deleteOnExit();
			out = new FileOutputStream(tempFile);
			IOUtils.copy(in, out);
			return tempFile;
		} catch (IOException e) {
			return null;
		} finally {
			if (in != null) {
				IOUtils.closeQuietly(in);
			}
			if (out != null) {
				IOUtils.closeQuietly(out);
			}
		}
	}

	public static <T extends Annotation> T getAnnotation(Class<?> klass, Class<T> annotationClass) {
		return klass.getAnnotation(annotationClass);
	}
}

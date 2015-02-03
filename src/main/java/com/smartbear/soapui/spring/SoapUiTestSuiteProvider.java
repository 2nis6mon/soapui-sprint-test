package com.smartbear.soapui.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import org.apache.commons.lang.StringEscapeUtils;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class SoapUiTestSuiteProvider {

	private final Class<?> generatedTestClass;
	private final Class<?> superClassTest;
	private final List<Method> generatedTestMethods;
	private final List<SoapUiTestCase> allTests;
	private final WsdlProjectPro project;

	public SoapUiTestSuiteProvider(Class<?> clazz) {
		try {

			SoapUiProject soapUiProjectAnnotation = SoapUiSpringTestUtils.getAnnotation(clazz, SoapUiProject.class);

			if (soapUiProjectAnnotation == null) {
				throw new SoapUiSpringTestException("Missing annotation \'@" + SoapUiProject.class.getSimpleName() + "\' on class ["
						+ clazz.getName() + "]");
			}

			project = SoapUiSpringTestUtils.crateWsdlProjectPro(clazz);
			allTests = SoapUiSpringTestUtils.suite(project);

			superClassTest = clazz;
			generatedTestClass = generateSoapUiProjectTestClass(clazz, allTests);
			generatedTestMethods = getSoapUiProjectTestClassMethods(generatedTestClass, allTests);

		} catch (Exception e) {
			if (SoapUiSpringTestException.class.isInstance(e)) {
				throw (SoapUiSpringTestException) e;
			}

			throw new SoapUiSpringTestException(e);
		}
	}

	static Class<?> generateSoapUiProjectTestClass(Class<?> superClazz, List<SoapUiTestCase> allTests) throws Exception {

		ClassPool pool = ClassPool.getDefault();
		CtClass newClazz = pool.makeClass(superClazz.getName() + ".generated" + System.nanoTime());
		CtClass superClass = pool.getCtClass(superClazz.getCanonicalName());
		newClazz.setSuperclass(superClass);

		for (SoapUiTestCase soapUiTestCase : allTests) {
			CtMethod m = createTestMethod(newClazz, soapUiTestCase);
			newClazz.addMethod(m);
		}

		Class<?> generatedTestClass = newClazz.toClass(ClassLoader.getSystemClassLoader(), null);

		return generatedTestClass;
	}

	static List<Method> getSoapUiProjectTestClassMethods(Class<?> generatedClass, List<SoapUiTestCase> allTests) throws Exception {
		List<Method> generatedTestMethods = new ArrayList<Method>();
		for (SoapUiTestCase soapUiTestCase : allTests) {
			Method m = generatedClass.getMethod(soapUiTestCase.getName());
			generatedTestMethods.add(m);
		}
		return generatedTestMethods;
	}

	private static CtMethod createTestMethod(CtClass newClass, SoapUiTestCase soapUiTestCase) throws CannotCompileException {
		String methodName = soapUiTestCase.getName();
		CtMethod m = new CtMethod(CtClass.voidType, methodName, new CtClass[0], newClass);
		m.setBody("launchTest(\"" + StringEscapeUtils.escapeJava(soapUiTestCase.getUniqueId()) + "\");");

		return m;
	}

	public List<Method> getTestMethods() {
		return generatedTestMethods;
	}

	public Object newTestClassInstance() throws Exception {
		Object testInstance = generatedTestClass.newInstance();
		if (testInstance instanceof SoapUiSpringTest) {
			SoapUiSpringTest test = (SoapUiSpringTest) testInstance;
			test.setReader(this);
			return test;
		}
		throw new SoapUiSpringTestException("Class " + superClassTest.getCanonicalName() + " must extends "
				+ SoapUiSpringTest.class.getCanonicalName());
	}

	public SoapUiTestCase getTest(final String id, final Map<String, String> properties) throws Throwable {
		SoapUiSpringTestUtils.setWsdlProjectProProperties(project, properties);
		Collection<SoapUiTestCase> filtered = Collections2.filter(allTests, new Predicate<SoapUiTestCase>() {

			public boolean apply(SoapUiTestCase input) {
				return id.equals(input.getUniqueId());
			}

		});

		if (filtered.size() == 0 || filtered.size() > 1) {
			throw new SoapUiSpringTestException("Test not found " + id);
		}
		return filtered.iterator().next();

	}
}

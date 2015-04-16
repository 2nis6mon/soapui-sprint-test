package com.smartbear.soapui.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Ignore;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class SoapUiTestSuiteProvider {

	private final Class<? extends SoapUiSpringTest> generatedTestClass;
	private final List<Method> generatedTestMethods;
	private final List<SoapUiTestCase> allTests;
	private final WsdlProjectPro project;

	public SoapUiTestSuiteProvider(Class<? extends SoapUiSpringTest> clazz) {
		try {

			project = SoapUiSpringTestUtils.createWsdlProjectPro(clazz);
			allTests = SoapUiSpringTestUtils.getSoapUiTestCases(project);

			IgnoreTestCase ignoreTestCase = clazz.getAnnotation(IgnoreTestCase.class);
			if (ignoreTestCase != null && ignoreTestCase.value().length == 0) {
				throw new SoapUiSpringTestException("Empty mandatory value \'@" + IgnoreTestCase.class.getSimpleName() + "\' on class ["
						+ clazz.getName() + "]");
			}
			List<String> ignoreTestCases = Collections.emptyList();
			if (ignoreTestCase != null) {
				ignoreTestCases = Arrays.asList(ignoreTestCase.value());
			}

			generatedTestClass = generateSoapUiProjectTestClass(clazz, allTests, ignoreTestCases);
			generatedTestMethods = getSoapUiProjectTestClassMethods(generatedTestClass, allTests);

		} catch (Exception e) {
			if (SoapUiSpringTestException.class.isInstance(e)) {
				throw (SoapUiSpringTestException) e;
			}

			throw new SoapUiSpringTestException(e);
		}
	}

	@SuppressWarnings("unchecked")
	static Class<? extends SoapUiSpringTest> generateSoapUiProjectTestClass(Class<? extends SoapUiSpringTest> superClazz,
			List<SoapUiTestCase> allTests, Collection<String> ignoreTestCases) throws Exception {

		ClassPool pool = ClassPool.getDefault();
		CtClass newClazz = pool.makeClass(superClazz.getName() + ".generated" + System.nanoTime());
		CtClass superClass = pool.getCtClass(superClazz.getCanonicalName());
		newClazz.setSuperclass(superClass);

		for (SoapUiTestCase soapUiTestCase : allTests) {
			CtMethod m = createTestMethod(newClazz, soapUiTestCase);
			newClazz.addMethod(m);

			if (ignoreTestCases.contains(soapUiTestCase.getName())) {
				ClassFile ccFile = newClazz.getClassFile();
				ConstPool constpool = ccFile.getConstPool();
				AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
				Annotation annot = new Annotation(Ignore.class.getCanonicalName(), constpool);
				attr.addAnnotation(annot);
				m.getMethodInfo().addAttribute(attr);
			}
		}

		Class<? extends SoapUiSpringTest> generatedTestClass = newClazz.toClass(ClassLoader.getSystemClassLoader(), null);

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
		SoapUiSpringTest testInstance = generatedTestClass.newInstance();
		testInstance.setReader(this);
		return testInstance;
	}

	public SoapUiTestCase getTest(final String id, Map<String, String> properties) throws Throwable {
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

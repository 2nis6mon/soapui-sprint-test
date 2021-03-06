package com.smartbear.soapui.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class SoapUiTestSuiteProvider {

	private final Class<? extends SoapUiSpringTest> generatedTestClass;
	private final List<Method> generatedTestMethods;

	private static Map<Class<?>, List<WsdlProject>> projectMap = new HashMap<Class<?>, List<WsdlProject>>();
	private static Map<Class<?>, List<SoapUiTestCase>> allTestsMap = new HashMap<Class<?>, List<SoapUiTestCase>>();

	public SoapUiTestSuiteProvider(Class<? extends SoapUiSpringTest> clazz) {
		try {
			List<WsdlProject> projects = SoapUiSpringTestUtils.createWsdlProject(clazz);

			List<SoapUiTestCase> reallyAllTests = Lists.newArrayList();
			for (WsdlProject wsdlProjectPro : projects) {
				//projectMap.put(clazz, wsdlProjectPro);
				List<SoapUiTestCase> allTests = SoapUiSpringTestUtils.getSoapUiTestCases(wsdlProjectPro);
				reallyAllTests.addAll(allTests);
				allTestsMap.put(clazz, allTests);
			}
			// NEW
			projectMap.put(clazz, projects);
			allTestsMap.put(clazz, reallyAllTests);
			// END NEW
			Collection<String> ignoreTestCases = createIgnoreTestCasesList(clazz);

			generatedTestClass = generateSoapUiProjectTestClass(clazz, reallyAllTests, ignoreTestCases);
			generatedTestMethods = getSoapUiProjectTestClassMethods(generatedTestClass, reallyAllTests);

		} catch (Exception e) {
			if (SoapUiSpringTestException.class.isInstance(e)) {
				throw (SoapUiSpringTestException) e;
			}

			throw new SoapUiSpringTestException(e);
		}
	}

	public static void clear(Class<? extends SoapUiSpringTest> clazz) {
		for (SoapUiTestCase test : allTestsMap.get(clazz)) {
			test.clear();
		}
		allTestsMap.remove(clazz);

		//		WsdlProjectPro project = projectMap.get(clazz);
		//		project.getWorkspace().closeProject(project);
		//		projectMap.remove(clazz);

		List<WsdlProject> projects = projectMap.get(clazz);
		for (WsdlProject wsdlProjectPro : projects) {
			wsdlProjectPro.getWorkspace().closeProject(wsdlProjectPro);
		}
		projectMap.remove(clazz);
	}

	public Collection<String> createIgnoreTestCasesList(Class<? extends SoapUiSpringTest> clazz) {
		IgnoreTestCase ignoreTestCase = clazz.getAnnotation(IgnoreTestCase.class);
		if (ignoreTestCase != null && ignoreTestCase.value().length == 0) {
			throw new SoapUiSpringTestException("Empty mandatory value \'@" + IgnoreTestCase.class.getSimpleName() + "\' on class ["
					+ clazz.getName() + "]");
		}
		Collection<String> ignoreTestCases = Collections.emptyList();
		if (ignoreTestCase != null) {
			ignoreTestCases = Arrays.asList(ignoreTestCase.value());
		}

		IgnoreAllTestCasesExcluding ignoreAllTestCaseExcept = clazz.getAnnotation(IgnoreAllTestCasesExcluding.class);
		if (ignoreAllTestCaseExcept != null && ignoreAllTestCaseExcept.value().length == 0) {
			throw new SoapUiSpringTestException("Empty mandatory value \'@" + IgnoreAllTestCasesExcluding.class.getSimpleName() + "\' on class ["
					+ clazz.getName() + "]");
		}
		if (ignoreAllTestCaseExcept != null && ignoreTestCase != null) {
			throw new SoapUiSpringTestException("Do not use '@" + IgnoreTestCase.class.getSimpleName() + "\' and '@"
					+ IgnoreAllTestCasesExcluding.class.getSimpleName() + "\' on the same class [" + clazz.getName() + "]");
		}

		if (ignoreAllTestCaseExcept != null) {
			ignoreTestCases = new ArrayList<String>();
			final List<String> excludeFromIgnore = Arrays.asList(ignoreAllTestCaseExcept.value());

			Collection<SoapUiTestCase> allTests = allTestsMap.get(clazz);
			Collection<String> result = Collections2.transform(allTests, new Function<SoapUiTestCase, String>() {

				public String apply(SoapUiTestCase soapUiTestCase) {
					return soapUiTestCase.getName();
				}
			});

			ignoreTestCases = Collections2.filter(result, new Predicate<String>() {

				public boolean apply(String testName) {
					return !excludeFromIgnore.contains(testName);
				}
			});
		}

		return ignoreTestCases;
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

	public SoapUiTestCase getTest(Class<?> clazz, final String id, Map<String, String> properties) throws Throwable {
		List<WsdlProject> projects = projectMap.get(clazz);
		for (WsdlProject wsdlProjectPro : projects) {
			SoapUiSpringTestUtils.setWsdlProjectProProperties(wsdlProjectPro, properties);
		}
		//	WsdlProjectPro project = projectMap.get(clazz);
		//  SoapUiSpringTestUtils.setWsdlProjectProProperties(project, properties);
		Collection<SoapUiTestCase> allTests = allTestsMap.get(clazz);
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

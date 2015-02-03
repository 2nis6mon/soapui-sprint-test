package com.smartbear.soapui.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class SoapUiSpringTestRunner extends SpringJUnit4ClassRunner {

	SoapUiTestSuiteProvider soapUiTestProvider;

	public SoapUiSpringTestRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		if (soapUiTestProvider == null) {
			soapUiTestProvider = new SoapUiTestSuiteProvider((Class<? extends SoapUiSpringTest>) this.getTestClass().getJavaClass());
		}
		List<FrameworkMethod> list = new ArrayList<FrameworkMethod>();
		for (Method testMethod : soapUiTestProvider.getTestMethods()) {
			list.add(new FrameworkMethod(testMethod));
		}

		return list;
	}

	@Override
	protected Object createTest() throws Exception {
		Object testInstance = soapUiTestProvider.newTestClassInstance();
		getTestContextManager().prepareTestInstance(testInstance);
		return testInstance;
	}

}

package com.smartbear.soapui.spring;

public class SoapUiSpringTestException extends RuntimeException {

	private static final long serialVersionUID = -8995128588377279593L;

	public SoapUiSpringTestException() {
	}

	public SoapUiSpringTestException(String message) {
		super(message);
	}

	public SoapUiSpringTestException(String message, Throwable cause) {
		super(message, cause);
	}

	public SoapUiSpringTestException(Throwable cause) {
		super(cause);
	}
}

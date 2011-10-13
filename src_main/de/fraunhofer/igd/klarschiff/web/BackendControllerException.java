package de.fraunhofer.igd.klarschiff.web;

import java.text.DecimalFormat;

@SuppressWarnings("serial")
public class BackendControllerException extends Exception {
	
	private int code;
	
	public BackendControllerException(int code, String message, Throwable exception)
	{
		super(message, exception);
		this.code = code;
	}
	public BackendControllerException(int code, String message)
	{
		super(message);
		this.code = code;
	}
	
	@Override
	public String getMessage() {
		return new DecimalFormat("000").format(code) + " - " + super.getMessage();
	}
}

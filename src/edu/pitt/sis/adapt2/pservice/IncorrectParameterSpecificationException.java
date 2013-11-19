package edu.pitt.sis.adapt2.pservice;

public class IncorrectParameterSpecificationException extends Exception
{
	static final long serialVersionUID = -2L;
	public IncorrectParameterSpecificationException()
	{
		super();
	}
	
	public IncorrectParameterSpecificationException(String s)
	{
		super(s);
	}
}

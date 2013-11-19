package edu.pitt.sis.adapt2.pservice;

public class UnrecognizedURITypeException extends Exception
{
	static final long serialVersionUID = -2L;
	public UnrecognizedURITypeException()
	{
		super();
	}
	
	public UnrecognizedURITypeException(String s)
	{
		super(s);
	}
}

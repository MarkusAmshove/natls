package org.amshove.natparse.parsing.project;

public class BuildFileParserException extends RuntimeException
{
	public BuildFileParserException(Exception cause)
	{
		super(cause);
	}

	public BuildFileParserException(String message)
	{
		super(message);
	}
}

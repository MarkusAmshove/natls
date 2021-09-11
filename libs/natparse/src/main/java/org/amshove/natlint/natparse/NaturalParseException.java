package org.amshove.natlint.natparse;

public class NaturalParseException extends RuntimeException
{
	public NaturalParseException(String message)
	{
		super(message);
	}

	public NaturalParseException(String message, int line)
	{
		super(String.format("Error at line %d: %s", line, message));
	}

	public NaturalParseException(Exception cause, int line)
	{
		super(String.format("Error at line %d: %s: %s", line, cause.getClass().getName(), cause.getMessage()), cause);
	}
}

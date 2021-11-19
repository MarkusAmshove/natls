package org.amshove.natparse.parsing;

public enum ParserError
{
	NO_DEFINE_DATA_FOUND("NPP001"),
	MISSING_END_DEFINE("NPP002");

	private final String id;

	private ParserError(String id)
	{
		this.id = id;
	}

	public String id()
	{
		return id;
	}
}

package org.amshove.natparse.parsing;

public enum ParserError
{
	NO_DEFINE_DATA_FOUND("NPP001"),
	MISSING_END_DEFINE("NPP002"),
	UNEXPECTED_TOKEN("NPP003"),
	INVALID_DATA_TYPE_FOR_DYNAMIC_LENGTH("NPP004"),
	VARIABLE_LENGTH_MISSING("NPP005"),
	INITIAL_VALUE_TYPE_MISMATCH("NPP006"),
	EMPTY_INITIAL_VALUE("NPP007")
	;

    private final String id;

	ParserError(String id)
	{
		this.id = id;
	}

	public String id()
	{
		return id;
	}
}

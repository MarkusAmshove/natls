package org.amshove.natparse.lexing;

public enum LexerError
{
	UNKNOWN_CHARACTER("NPL001"),
	UNTERMINATED_STRING("NPL002"),
	INVALID_IDENTIFIER("NPL003"),
	INVALID_STRING_LENGTH("NPL004"),
	MISSING_COPYCODE_PARAMETER("NPL005"),
	UNRESOLVED_COPYCODE("NPL006"),
	INVALID_INCLUDE_TYPE("NPL007"),
	CYCLOMATIC_INCLUDE("NPL008");

	private final String id;

	LexerError(String id)
	{
		this.id = id;
	}

	public String id()
	{
		return id;
	}
}

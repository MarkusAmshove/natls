package org.amshove.natparse.lexing;

public enum LexerError
{
	UNKNOWN_CHARACTER("NPL001"),
	UNTERMINATED_STRING("NPL002");

	private String id;

	private LexerError(String id)
	{
		this.id = id;
	}

	public String id()
	{
		return id;
	}
}

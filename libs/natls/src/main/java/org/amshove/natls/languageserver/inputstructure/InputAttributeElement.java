package org.amshove.natls.languageserver.inputstructure;

public class InputAttributeElement
{
	private final String kind;
	private final String value;

	public InputAttributeElement(String kind, String value)
	{
		this.kind = kind;
		this.value = value;
	}

	public String getKind()
	{
		return kind;
	}

	public String getValue()
	{
		return value;
	}
}

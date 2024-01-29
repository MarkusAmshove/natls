package org.amshove.natls.languageserver.inputstructure;

public class InputStructureParams
{
	private String uri;
	private int inputIndex;

	public void setUri(String uri)
	{
		this.uri = uri;
	}

	public String getUri()
	{
		return this.uri;
	}

	public int getInputIndex()
	{
		return inputIndex;
	}

	public void setInputIndex(int inputIndex)
	{
		this.inputIndex = inputIndex;
	}
}

package org.amshove.natls.languageserver.inputstructure;

public class InputStructureParams
{
	private String uri;
	private int inputPosition;

	public void setUri(String uri)
	{
		this.uri = uri;
	}

	public String getUri()
	{
		return this.uri;
	}

	public int getInputPosition()
	{
		return inputPosition;
	}

	public void setInputPosition(int inputPosition)
	{
		this.inputPosition = inputPosition;
	}
}

package org.amshove.natls.languageserver.inputstructure;

public class InputSpaceElement extends InputResponseElement
{
	private int spaces;

	protected InputSpaceElement(int spaces)
	{
		super(InputStructureElementKind.SPACES);
		this.spaces = spaces;
	}

	public int getSpaces()
	{
		return spaces;
	}

	public void setSpaces(int spaces)
	{
		this.spaces = spaces;
	}
}

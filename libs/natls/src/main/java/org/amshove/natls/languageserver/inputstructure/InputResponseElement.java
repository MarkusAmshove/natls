package org.amshove.natls.languageserver.inputstructure;

import org.amshove.natparse.natural.output.*;

public class InputResponseElement
{
	private final String kind;
	private int id;

	protected InputResponseElement(String kind)
	{
		this.kind = kind;
	}

	public static InputResponseElement fromOutputElement(IOutputElementNode element)
	{
		if (element instanceof IOutputOperandNode operandNode)
		{
			return new InputOperandElement(operandNode);
		}

		if (element instanceof IOutputNewLineNode)
		{
			return new InputResponseElement("newline");
		}

		if (element instanceof ISpaceElementNode spaceElement)
		{
			return new InputSpaceElement(spaceElement.spaces());
		}

		if (element instanceof ITabulatorElementNode tabElement)
		{
			return new InputTabElement(tabElement.tabs());
		}

		return null;
	}

	public String getKind()
	{
		return kind;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
}

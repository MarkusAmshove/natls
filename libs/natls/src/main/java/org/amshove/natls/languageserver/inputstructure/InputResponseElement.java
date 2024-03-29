package org.amshove.natls.languageserver.inputstructure;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAttributeNode;
import org.amshove.natparse.natural.output.*;

public class InputResponseElement
{
	private final String kind;
	private int id;

	protected InputResponseElement(String kind)
	{
		this.kind = kind;
	}

	public static InputResponseElement fromOutputElement(IOutputElementNode element, ReadOnlyList<IAttributeNode> statementAttributes)
	{
		if (element instanceof IOutputOperandNode operandNode)
		{
			return new InputOperandElement(operandNode, statementAttributes);
		}

		if (element instanceof IOutputNewLineNode)
		{
			return new InputResponseElement(InputStructureElementKind.NEW_LINE);
		}

		if (element instanceof ISpaceElementNode spaceElement)
		{
			return new InputSpaceElement(spaceElement.spaces());
		}

		if (element instanceof ITabulatorElementNode tabElement)
		{
			return new InputColumnPositionElement(tabElement.tabs());
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

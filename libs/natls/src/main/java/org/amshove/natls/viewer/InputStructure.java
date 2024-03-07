package org.amshove.natls.viewer;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IAttributeNode;
import org.amshove.natparse.natural.output.IOutputElementNode;

import java.util.List;

public class InputStructure
{
	private List<IOutputElementNode> operands;
	private ReadOnlyList<IAttributeNode> attributes;

	public void setOperands(ReadOnlyList<IOutputElementNode> operands)
	{
		this.operands = operands.toList();
	}

	public List<IOutputElementNode> getOperands()
	{
		return operands;
	}

	public void setAttributes(ReadOnlyList<IAttributeNode> attributes)
	{
		this.attributes = attributes;
	}

	public ReadOnlyList<IAttributeNode> statementAttributes()
	{
		return attributes;
	}
}

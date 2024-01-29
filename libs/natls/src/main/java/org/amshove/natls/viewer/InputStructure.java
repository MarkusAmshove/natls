package org.amshove.natls.viewer;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.output.IOutputElementNode;

import java.util.List;

public class InputStructure
{
	private List<IOutputElementNode> operands;

	public void setOperands(ReadOnlyList<IOutputElementNode> operands)
	{
		this.operands = operands.toList();
	}

	public List<IOutputElementNode> getOperands()
	{
		return operands;
	}
}

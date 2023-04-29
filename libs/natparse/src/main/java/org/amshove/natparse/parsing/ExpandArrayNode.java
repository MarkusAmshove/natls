package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IExpandArrayNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class ExpandArrayNode extends StatementNode implements IExpandArrayNode
{
	private IVariableReferenceNode arrayToExpand;

	@Override
	public IVariableReferenceNode arrayToExpand()
	{
		return arrayToExpand;
	}

	void setArrayToExpand(IVariableReferenceNode array)
	{
		arrayToExpand = array;
	}
}

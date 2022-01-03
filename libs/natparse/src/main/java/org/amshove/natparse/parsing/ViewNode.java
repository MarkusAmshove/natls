package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IViewNode;

class ViewNode extends GroupNode implements IViewNode
{
	private SyntaxToken ddmNameToken;

	public ViewNode(VariableNode variable)
	{
		super(variable);
	}

	void setDdmNameToken(SyntaxToken ddmNameToken)
	{
		this.ddmNameToken = ddmNameToken;
	}

	@Override
	public SyntaxToken ddmNameToken()
	{
		return ddmNameToken;
	}
}

package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IViewNode;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;

class ViewNode extends GroupNode implements IViewNode
{
	private SyntaxToken ddmNameToken;
	private IDataDefinitionModule ddm;

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

	@Override
	public IDataDefinitionModule ddm()
	{
		return ddm;
	}

	@Override
	public boolean isInView()
	{
		return false;
	}

	void setDdm(IDataDefinitionModule ddm)
	{
		this.ddm = ddm;
	}
}

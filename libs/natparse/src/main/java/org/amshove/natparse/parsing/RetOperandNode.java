package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IModuleReferencingNode;
import org.amshove.natparse.natural.IRetOperandNode;

class RetOperandNode extends BaseSyntaxNode implements IRetOperandNode
{
	private IModuleReferencingNode reference;

	@Override
	public IModuleReferencingNode reference()
	{
		return reference;
	}

	void setReference(IModuleReferencingNode referencingNode)
	{
		this.reference = referencingNode;
	}
}

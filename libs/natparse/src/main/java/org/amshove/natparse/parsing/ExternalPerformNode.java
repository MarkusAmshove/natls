package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IExternalPerformNode;
import org.amshove.natparse.natural.INaturalModule;

final class ExternalPerformNode extends PerformNode implements IExternalPerformNode
{

	private final SyntaxToken referencingToken;
	private INaturalModule reference;

	public ExternalPerformNode(InternalPerformNode internalPerformNode)
	{
		copyFrom(internalPerformNode);
		referencingToken = internalPerformNode.token();
	}

	@Override
	public INaturalModule reference()
	{
		return reference;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return referencingToken;
	}

	void setReference(INaturalModule module)
	{
		module.addCaller(this);
		reference = module;
	}
}

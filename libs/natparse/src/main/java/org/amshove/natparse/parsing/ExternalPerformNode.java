package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IExternalPerformNode;
import org.amshove.natparse.natural.INaturalModule;

final class ExternalPerformNode extends PerformNode implements IExternalPerformNode
{
	@Override
	public INaturalModule reference()
	{
		return null;
	}

	@Override
	public SyntaxToken referencingToken()
	{
		return null;
	}
}
